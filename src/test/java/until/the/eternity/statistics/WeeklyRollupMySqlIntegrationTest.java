package until.the.eternity.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
import until.the.eternity.statistics.repository.weekly.ItemWeeklyStatisticsRepository;
import until.the.eternity.statistics.repository.weekly.SubcategoryWeeklyStatisticsRepository;
import until.the.eternity.statistics.repository.weekly.TopCategoryWeeklyStatisticsRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslConfig.class)
@Testcontainers(disabledWithoutDocker = true)
class WeeklyRollupMySqlIntegrationTest {
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

    @Autowired JdbcTemplate jdbc;
    @Autowired ItemWeeklyStatisticsRepository items;
    @Autowired SubcategoryWeeklyStatisticsRepository subs;
    @Autowired TopCategoryWeeklyStatisticsRepository tops;

    @Test
    void preservesFullIdentitiesAndConservesTotalsOnRepeat() {
        trade("1", "same", "T1", "shared", 0, 10, 2);
        trade("2", "same", "T1", "shared", 0, 20, 1);
        trade("3", "same", "T1", "shared", 1, 25, 2);
        trade("4", "same", "T2", "shared", 0, 90, 1);
        trade("5", "other", "T1", "distinct", 6, 60, 1);
        trade("6", "outside", "T1", "shared", 7, 1000, 1);
        trade("7", "outside", "T1", "shared", -1, 1000, 1);
        for (int run = 0; run < 2; run++) {
            items.upsertWeeklyStatistics();
            subs.upsertWeeklyStatistics();
            tops.upsertWeeklyStatistics();
            assertThat(scalar("COUNT(*) FROM item_weekly_statistics")).isEqualTo(3);
            assertThat(
                            scalar(
                                    "total_volume FROM subcategory_weekly_statistics WHERE item_sub_category='shared'"))
                    .isEqualTo(180);
            assertThat(
                            scalar(
                                    "total_volume FROM top_category_weekly_statistics WHERE item_top_category='T1'"))
                    .isEqualTo(150);
            assertThat(
                            scalar(
                                    "avg_price FROM top_category_weekly_statistics WHERE item_top_category='T1'"))
                    .isEqualTo(40);
            for (String table :
                    new String[] {
                        "item_weekly_statistics",
                        "subcategory_weekly_statistics",
                        "top_category_weekly_statistics"
                    }) {
                assertThat(scalar("SUM(total_volume) FROM " + table)).isEqualTo(240);
                assertThat(scalar("SUM(total_quantity) FROM " + table)).isEqualTo(7);
            }
        }
    }

    private long scalar(String select) {
        return jdbc.queryForObject("SELECT " + select, Long.class);
    }

    private void trade(
            String id, String name, String top, String sub, int day, int price, int qty) {
        jdbc.update(
                """
                INSERT INTO auction_history (auction_buy_id,item_name,item_display_name,item_top_category,item_sub_category,
                date_auction_buy,auction_price_per_unit,item_count) VALUES (?,?,?,?,?,
                DATE_ADD(DATE_SUB(DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY), INTERVAL ? DAY),?,?)
                """,
                id,
                name,
                name,
                top,
                sub,
                day,
                price,
                qty);
    }
}
