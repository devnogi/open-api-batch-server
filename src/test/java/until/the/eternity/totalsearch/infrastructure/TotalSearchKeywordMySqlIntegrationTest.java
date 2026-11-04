package until.the.eternity.totalsearch.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
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
import until.the.eternity.totalsearch.application.TotalSearchKeywordSyncService;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TotalSearchKeywordSyncService.class)
@Testcontainers(disabledWithoutDocker = true)
class TotalSearchKeywordMySqlIntegrationTest {

    private static final int MYSQL_PORT = 3306;

    @Container
    static final GenericContainer<?> MYSQL =
            new GenericContainer<>(DockerImageName.parse("mysql:8.4"))
                    .withEnv("MYSQL_DATABASE", "devnogi")
                    .withEnv("MYSQL_USER", "devnogi")
                    .withEnv("MYSQL_PASSWORD", "devnogi")
                    .withEnv("MYSQL_ROOT_PASSWORD", "root")
                    .withExposedPorts(MYSQL_PORT)
                    .withStartupTimeout(Duration.ofMinutes(2));

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () ->
                        "jdbc:mysql://"
                                + MYSQL.getHost()
                                + ":"
                                + MYSQL.getMappedPort(MYSQL_PORT)
                                + "/devnogi?useSSL=false&allowPublicKeyRetrieval=true");
        registry.add("spring.datasource.username", () -> "devnogi");
        registry.add("spring.datasource.password", () -> "devnogi");
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private TotalSearchKeywordSyncService syncService;

    @Test
    @DisplayName("MySQL 8 Flyway와 동기화 SQL을 검증한다 (Docker 미사용 시 건너뜀)")
    void migrationsAndItemSynchronizationAreIdempotentAndKeepMenusIsolated() {
        Integer menuCount =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM total_search_keyword WHERE keyword_type = 'MENU'",
                        Integer.class);
        assertThat(menuCount).isEqualTo(11);

        jdbcTemplate.update("DELETE FROM item_info");
        jdbcTemplate.update("DELETE FROM total_search_keyword WHERE keyword_type = 'ITEM'");
        insertItem("중복 아이템", "검", "근거리 장비");
        insertItem("중복 아이템", "도끼", "근거리 장비");
        insertItem("단일 아이템", "기타", "기타");

        syncService.synchronizeItems();
        syncService.synchronizeItems();

        assertThat(itemKeywords()).containsExactlyInAnyOrder("중복 아이템", "단일 아이템");
        assertThat(menuCount()).isEqualTo(11);

        jdbcTemplate.update(
                "DELETE FROM item_info WHERE name = ? AND sub_category = ?", "중복 아이템", "검");
        syncService.synchronizeItems();
        assertThat(itemKeywords()).containsExactlyInAnyOrder("중복 아이템", "단일 아이템");

        jdbcTemplate.update("DELETE FROM item_info WHERE name = ?", "중복 아이템");
        syncService.synchronizeItems();

        assertThat(itemKeywords()).containsExactly("단일 아이템");
        assertThat(menuCount()).isEqualTo(11);
    }

    private void insertItem(String name, String subCategory, String topCategory) {
        jdbcTemplate.update(
                "INSERT INTO item_info (name, sub_category, top_category) VALUES (?, ?, ?)",
                name,
                subCategory,
                topCategory);
    }

    private java.util.List<String> itemKeywords() {
        return jdbcTemplate.queryForList(
                "SELECT keyword FROM total_search_keyword WHERE keyword_type = 'ITEM' ORDER BY keyword",
                String.class);
    }

    private int menuCount() {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM total_search_keyword WHERE keyword_type = 'MENU'",
                        Integer.class);
        return count == null ? 0 : count;
    }
}
