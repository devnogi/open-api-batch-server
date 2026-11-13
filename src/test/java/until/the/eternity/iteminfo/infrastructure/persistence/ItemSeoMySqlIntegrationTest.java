package until.the.eternity.iteminfo.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(statistics.getPrepareStatementCount()).isEqualTo(1);
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

    private void insert(String name, String top, String sub, String description) {
        jdbc.update(
                "INSERT INTO item_info(name,top_category,sub_category,description) VALUES (?,?,?,?)",
                name,
                top,
                sub,
                description);
    }
}
