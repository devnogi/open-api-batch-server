package until.the.eternity.iteminfo.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import until.the.eternity.config.QuerydslConfig;
import until.the.eternity.iteminfo.application.service.ItemSeoService;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({QuerydslConfig.class, ItemSeoRepository.class})
@Testcontainers(disabledWithoutDocker = true)
class ItemSeoMySqlIntegrationTest {
    @Container
    static final GenericContainer<?> MYSQL =
            new GenericContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withEnv("MYSQL_DATABASE", "devnogi")
                    .withEnv("MYSQL_USER", "devnogi")
                    .withEnv("MYSQL_PASSWORD", "devnogi")
                    .withEnv("MYSQL_ROOT_PASSWORD", "root")
                    .withExposedPorts(3306)
                    .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () ->
                        "jdbc:mysql://"
                                + MYSQL.getHost()
                                + ":"
                                + MYSQL.getMappedPort(3306)
                                + "/devnogi?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", () -> "devnogi");
        registry.add("spring.datasource.password", () -> "devnogi");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private ItemSeoRepository repository;
    @Autowired private EntityManagerFactory entityManagerFactory;

    @Test
    void sitemapIndexMigrationIsIdempotentAndKeepsIdentityOrder() {
        jdbc.execute(
                (org.springframework.jdbc.core.ConnectionCallback<Void>)
                        connection -> {
                            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(
                                    connection,
                                    new org.springframework.core.io.ClassPathResource(
                                            "db/migration/V34__add_item_sitemap_identity_index.sql"));
                            return null;
                        });
        assertThat(
                        jdbc.queryForList(
                                "SELECT COLUMN_NAME FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='item_info' AND index_name='idx_item_info_sitemap_identity' ORDER BY SEQ_IN_INDEX",
                                String.class))
                .containsExactly("name", "top_category", "sub_category");
    }

    @Test
    void retainedStatisticsAndMetadataShareEligibilityWithExclusiveCompositeCursor() {
        jdbc.update("DELETE FROM item_info");
        insert("A", "A", "A", "details");
        insert("A", "A", "B", "\t\n ");
        insert("A", "B", "A", null);
        insert("B", "A", "A", null);
        insert("C", "A", "A", null);
        jdbc.update(
                "INSERT INTO item_daily_statistics (item_name,item_top_category,item_sub_category,date_auction_buy,min_price,max_price,avg_price,total_volume,total_quantity,created_at,updated_at) VALUES ('A','A','B','2020-01-01',100,500,500,1000,3,NOW(),NOW())");
        jdbc.update(
                "INSERT INTO item_weekly_statistics (item_name,item_top_category,item_sub_category,year,week_number,week_start_date,min_price,max_price,avg_price,total_volume,total_quantity,created_at,updated_at) VALUES ('B','A','A',2020,1,'2019-12-30',100,500,500,1000,3,NOW(),NOW())");
        assertThat(repository.findSeoItem(new ItemInfoId("A", "A", "B")).orElseThrow().eligible())
                .isFalse();
        assertThat(repository.findSeoItem(new ItemInfoId("A", "B", "A")).orElseThrow().eligible())
                .isTrue();
        assertThat(repository.findSeoItem(new ItemInfoId("B", "A", "A")).orElseThrow().eligible())
                .isTrue();
        assertThat(repository.findSeoItem(new ItemInfoId("C", "A", "A")).orElseThrow().eligible())
                .isFalse();
        assertThat(
                        repository
                                .findLastDaily(new ItemInfoId("A", "B", "A"))
                                .orElseThrow()
                                .getDateAuctionBuy())
                .hasToString("2020-01-01");
        assertThat(repository.findLastDaily(new ItemInfoId("A", "A", "B"))).isEmpty();
        assertThat(
                        repository
                                .findLastWeekly(new ItemInfoId("B", "A", "A"))
                                .orElseThrow()
                                .getWeekStartDate())
                .hasToString("2019-12-30");
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var first = repository.findSitemapItems(null, 2);
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(2);
        assertThat(first).hasSize(2);
        assertThat(first.get(0).subCategory()).isEqualTo("A");
        assertThat(first.get(1).subCategory()).isEqualTo("B");
        var next = repository.findSitemapItems(new ItemInfoId("A", "B", "A"), 2);
        assertThat(next).extracting(value -> value.name()).containsExactly("B");
        jdbc.update(
                "UPDATE item_info SET description = '\t\n ' WHERE name='A' AND top_category='A' AND sub_category='A'");
        assertThat(repository.findSitemapItems(null, 10)).hasSize(2);
    }

    @Test
    void explainUsesExistingStatisticsIndexesOnSyntheticCatalog() {
        List<Object[]> rows = new ArrayList<>();
        for (int index = 0; index < 2000; index++) {
            rows.add(new Object[] {"synthetic" + index, "A", "A"});
        }
        jdbc.batchUpdate(
                "INSERT INTO item_info(name,top_category,sub_category) VALUES (?,?,?)", rows);
        String sql =
                "SELECT i.name,i.top_category,i.sub_category FROM item_info i WHERE (REGEXP_LIKE(i.description,'[^[:space:]]') OR REGEXP_LIKE(i.acquisition_method,'[^[:space:]]') OR REGEXP_LIKE(i.repair,'[^[:space:]]') OR REGEXP_LIKE(i.store_sales_price,'[^[:space:]]') OR EXISTS(SELECT 1 FROM item_daily_statistics d WHERE d.item_name=i.name AND d.item_top_category=i.top_category AND d.item_sub_category=i.sub_category) OR EXISTS(SELECT 1 FROM item_weekly_statistics w WHERE w.item_name=i.name AND w.item_top_category=i.top_category AND w.item_sub_category=i.sub_category)) AND i.name > 'synthetic100' ORDER BY i.name,i.top_category,i.sub_category LIMIT 1001";
        var plan = jdbc.queryForList("EXPLAIN " + sql);
        System.out.println("SEO synthetic MySQL EXPLAIN: " + plan);
        assertThat(plan)
                .anySatisfy(
                        row ->
                                assertThat(String.valueOf(row.get("possible_keys")))
                                        .contains("idx_item_daily_item_category_date"));
        assertThat(plan)
                .anySatisfy(
                        row ->
                                assertThat(String.valueOf(row.get("possible_keys")))
                                        .contains("idx_item_weekly_item_category_start_date"));
        assertThat(repository.findSitemapItems(new ItemInfoId("synthetic100", "A", "A"), 1001))
                .isEmpty();
    }

    @Test
    void windowsUseCoveringIndexAndMatchOriginalEligibilityForDenseCatalog() {
        List<Object[]> rows = new ArrayList<>();
        for (int index = 0; index < 10000; index++) {
            rows.add(
                    new Object[] {
                        "indexed" + String.format("%05d", index / 4),
                        "T" + index % 2,
                        "S" + index % 4,
                        index % 3 == 0 ? null : "details"
                    });
        }
        jdbc.batchUpdate(
                "INSERT INTO item_info(name,top_category,sub_category,description) VALUES (?,?,?,?)",
                rows);
        String select = "SELECT i.name,i.top_category,i.sub_category FROM item_info i ";
        String predicate =
                "(REGEXP_LIKE(i.description,'[^[:space:]]') OR REGEXP_LIKE(i.acquisition_method,'[^[:space:]]') OR REGEXP_LIKE(i.repair,'[^[:space:]]') OR REGEXP_LIKE(i.store_sales_price,'[^[:space:]]') OR EXISTS(SELECT 1 FROM item_daily_statistics d WHERE d.item_name=i.name AND d.item_top_category=i.top_category AND d.item_sub_category=i.sub_category) OR EXISTS(SELECT 1 FROM item_weekly_statistics w WHERE w.item_name=i.name AND w.item_top_category=i.top_category AND w.item_sub_category=i.sub_category))";
        String order = " ORDER BY i.name,i.top_category,i.sub_category";
        String cursor =
                "(i.name > 'indexed00250' OR (i.name = 'indexed00250' AND i.top_category > 'T0') OR (i.name = 'indexed00250' AND i.top_category = 'T0' AND i.sub_category > 'S0'))";
        for (String boundary : List.of("", cursor)) {
            String candidate =
                    select
                            + (boundary.isEmpty() ? "" : "WHERE " + boundary)
                            + order
                            + " LIMIT 1000";
            var candidates = jdbc.queryForList(candidate);
            var last = candidates.getLast();
            String end =
                    "(i.name < ? OR (i.name = ? AND i.top_category < ?) OR (i.name = ? AND i.top_category = ? AND i.sub_category <= ?))";
            Object[] endParameters = {
                last.get("name"),
                last.get("name"),
                last.get("top_category"),
                last.get("name"),
                last.get("top_category"),
                last.get("sub_category")
            };
            String window =
                    select
                            + "WHERE "
                            + predicate
                            + " AND "
                            + end
                            + (boundary.isEmpty() ? "" : " AND " + boundary)
                            + order
                            + " LIMIT 1001";
            String original =
                    select
                            + "IGNORE INDEX (idx_item_info_sitemap_identity) WHERE "
                            + predicate
                            + (boundary.isEmpty() ? "" : " AND " + boundary)
                            + order
                            + " LIMIT 1001";
            var candidatePlan = jdbc.queryForList("EXPLAIN " + candidate);
            var windowPlan = jdbc.queryForList("EXPLAIN " + window, endParameters);
            System.out.println(
                    "SEO baseline ANALYZE: " + jdbc.queryForList("EXPLAIN ANALYZE " + original));
            System.out.println(
                    "SEO identity-window ANALYZE: "
                            + jdbc.queryForList("EXPLAIN ANALYZE " + candidate));
            System.out.println(
                    "SEO eligibility-window ANALYZE: "
                            + jdbc.queryForList("EXPLAIN ANALYZE " + window, endParameters));
            assertThat(candidatePlan.getFirst().get("key"))
                    .isEqualTo("idx_item_info_sitemap_identity");
            assertThat(String.valueOf(candidatePlan.getFirst().get("Extra")))
                    .contains("Using index")
                    .doesNotContain("filesort");
            assertThat(windowPlan.getFirst().get("type")).isEqualTo("range");
            assertThat(((Number) windowPlan.getFirst().get("rows")).longValue())
                    .isLessThanOrEqualTo(1100);
            ItemInfoId after =
                    boundary.isEmpty() ? null : new ItemInfoId("indexed00250", "S0", "T0");
            var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
            statistics.clear();
            var actual = repository.findSitemapItems(after, 1001);
            assertThat(statistics.getPrepareStatementCount()).isEqualTo(4);
            var expected = jdbc.queryForList(original);
            assertThat(actual).hasSize(1001);
            for (int index = 0; index < actual.size(); index++) {
                assertThat(actual.get(index).name()).isEqualTo(expected.get(index).get("name"));
                assertThat(actual.get(index).topCategory())
                        .isEqualTo(expected.get(index).get("top_category"));
                assertThat(actual.get(index).subCategory())
                        .isEqualTo(expected.get(index).get("sub_category"));
            }
        }
    }

    @Test
    void skipsEntireIneligibleWindowsAndPreservesRetainedStatisticsContinuationAndEnd() {
        List<Object[]> rows = new ArrayList<>();
        for (int index = 0; index < 2505; index++) {
            rows.add(new Object[] {"sparse" + String.format("%05d", index), "T", "S"});
        }
        jdbc.batchUpdate(
                "INSERT INTO item_info(name,top_category,sub_category) VALUES (?,?,?)", rows);
        jdbc.update(
                "INSERT INTO item_daily_statistics (item_name,item_top_category,item_sub_category,date_auction_buy,min_price,max_price,avg_price,total_volume,total_quantity,created_at,updated_at) VALUES ('sparse02001','T','S','2020-01-01',100,500,500,1000,3,NOW(),NOW())");
        jdbc.update(
                "UPDATE item_info SET acquisition_method='retained information' WHERE name='sparse02504'");
        var service = new ItemSeoService(repository, new ObjectMapper());
        var statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
        var first =
                service.sitemap(
                        new until.the.eternity.iteminfo.interfaces.rest.dto.request
                                .ItemSitemapRequest(null, 1));
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(6);
        assertThat(first.items()).extracting(value -> value.name()).containsExactly("sparse02001");
        assertThat(first.hasNext()).isTrue();
        var next =
                service.sitemap(
                        new until.the.eternity.iteminfo.interfaces.rest.dto.request
                                .ItemSitemapRequest(first.nextCursor(), 1));
        assertThat(next.items()).extracting(value -> value.name()).containsExactly("sparse02504");
        assertThat(next.hasNext()).isFalse();
        assertThat(next.nextCursor()).isNull();
        assertThat(repository.findSitemapItems(new ItemInfoId("sparse02504", "S", "T"), 1001))
                .isEmpty();
        jdbc.update("DELETE FROM item_daily_statistics WHERE item_name='sparse02001'");
        jdbc.update("UPDATE item_info SET acquisition_method=NULL WHERE name='sparse02504'");
        assertThat(repository.findSitemapItems(null, 1001)).isEmpty();
    }

    private void insert(String name, String top, String sub, String description) {
        jdbc.update(
                "INSERT INTO item_info(name,top_category,sub_category,description) VALUES (?,?,?,?)",
                name,
                top,
                sub,
                description);
    }
}
