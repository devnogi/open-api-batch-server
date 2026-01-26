package until.the.eternity.hornBugle.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

public interface HornBugleRepositoryPort {

    void saveAll(List<HornBugleWorldHistory> entities);

    Optional<HornBugleWorldHistory> findLatestByServerName(String serverName);

    Page<HornBugleWorldHistory> findByServerName(String serverName, Pageable pageable);

    Page<HornBugleWorldHistory> findAll(Pageable pageable);

    List<HornBugleWorldHistory> findByServerNameAndDateSend(String serverName, Instant dateSend);
}
