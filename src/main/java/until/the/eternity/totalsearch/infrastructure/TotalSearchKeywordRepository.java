package until.the.eternity.totalsearch.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import until.the.eternity.totalsearch.domain.TotalSearchKeyword;

public interface TotalSearchKeywordRepository extends JpaRepository<TotalSearchKeyword, Long> {

    List<TotalSearchKeyword> findAllByOrderByKeywordAsc();

    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO total_search_keyword
                        (keyword, keyword_type, target_key, created_at, updated_at)
                    SELECT DISTINCT name, 'ITEM', name, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
                    FROM item_info
                    ON DUPLICATE KEY UPDATE
                        keyword = VALUES(keyword),
                        updated_at = CURRENT_TIMESTAMP(6)
                    """,
            nativeQuery = true)
    int upsertItemKeywords();

    @Modifying
    @Query(
            value =
                    """
                    DELETE FROM total_search_keyword
                    WHERE keyword_type = 'ITEM'
                      AND NOT EXISTS (
                          SELECT 1 FROM item_info
                          WHERE item_info.name = total_search_keyword.target_key
                      )
                    """,
            nativeQuery = true)
    int deleteMissingItemKeywords();
}
