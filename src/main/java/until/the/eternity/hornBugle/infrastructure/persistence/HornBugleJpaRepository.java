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
}
