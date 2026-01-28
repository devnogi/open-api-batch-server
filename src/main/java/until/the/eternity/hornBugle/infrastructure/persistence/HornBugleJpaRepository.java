package until.the.eternity.hornBugle.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HornBugleJpaRepository extends JpaRepository<HornBugleWorldHistory, Long> {

    @Query(
            """
            SELECT h FROM HornBugleWorldHistory h
            WHERE h.serverName = :serverName
            ORDER BY h.dateSend DESC
            LIMIT 1
            """)
    Optional<HornBugleWorldHistory> findLatestByServerName(String serverName);

    Page<HornBugleWorldHistory> findByServerName(String serverName, Pageable pageable);

    List<HornBugleWorldHistory> findByServerNameAndDateSend(String serverName, Instant dateSend);

    /** FULLTEXT 인덱스를 사용한 키워드 검색 (전체 서버) */
    @Query(
            value =
                    """
                    SELECT * FROM horn_bugle_world_history
                    WHERE MATCH(character_name, message, server_name, date_send_text) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
                    ORDER BY date_send DESC
                    """,
            countQuery =
                    """
                    SELECT COUNT(*) FROM horn_bugle_world_history
                    WHERE MATCH(character_name, message, server_name, date_send_text) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
                    """,
            nativeQuery = true)
    Page<HornBugleWorldHistory> searchByKeyword(String keyword, Pageable pageable);

    /** FULLTEXT 인덱스를 사용한 키워드 검색 (서버 필터 포함) */
    @Query(
            value =
                    """
                    SELECT * FROM horn_bugle_world_history
                    WHERE MATCH(character_name, message, server_name, date_send_text) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
                    AND server_name = :serverName
                    ORDER BY date_send DESC
                    """,
            countQuery =
                    """
                    SELECT COUNT(*) FROM horn_bugle_world_history
                    WHERE MATCH(character_name, message, server_name, date_send_text) AGAINST(:keyword IN NATURAL LANGUAGE MODE)
                    AND server_name = :serverName
                    """,
            nativeQuery = true)
    Page<HornBugleWorldHistory> searchByKeywordAndServerName(
            String keyword, String serverName, Pageable pageable);
}
