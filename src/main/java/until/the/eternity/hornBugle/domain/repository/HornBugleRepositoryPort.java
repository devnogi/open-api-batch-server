package until.the.eternity.hornBugle.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HornBugleRepositoryPort {

    void saveAll(List<HornBugleWorldHistory> entities);

    Optional<HornBugleWorldHistory> findLatestByServerName(String serverName);

    Page<HornBugleWorldHistory> findByServerName(String serverName, Pageable pageable);

    Page<HornBugleWorldHistory> findAll(Pageable pageable);

    Slice<HornBugleWorldHistory> findRecent(Pageable pageable);

    Slice<HornBugleWorldHistory> findRecentByServerName(String serverName, Pageable pageable);

    List<HornBugleWorldHistory> findByServerNameAndDateSend(String serverName, Instant dateSend);

    /** FULLTEXT 인덱스를 사용한 키워드 검색 (전체 서버) */
    Page<HornBugleWorldHistory> searchByKeyword(String keyword, Pageable pageable);

    /** FULLTEXT 인덱스를 사용한 키워드 검색 (서버 필터 포함) */
    Page<HornBugleWorldHistory> searchByKeywordAndServerName(
            String keyword, String serverName, Pageable pageable);
}
