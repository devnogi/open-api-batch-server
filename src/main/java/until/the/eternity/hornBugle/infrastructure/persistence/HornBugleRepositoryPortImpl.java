package until.the.eternity.hornBugle.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HornBugleRepositoryPortImpl implements HornBugleRepositoryPort {

    private final HornBugleJpaRepository jpaRepository;
    private final EntityManager em;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:500}")
    private int batchSize;

    @Override
    @Transactional
    public void saveAll(List<HornBugleWorldHistory> entities) {
        if (entities.isEmpty()) {
            return;
        }

        for (int i = 0; i < entities.size(); i += batchSize) {
            int toIndex = Math.min(i + batchSize, entities.size());
            List<HornBugleWorldHistory> subList = entities.subList(i, toIndex);
            jpaRepository.saveAll(subList);
            em.flush();
            em.clear();
        }
    }

    @Override
    public Optional<HornBugleWorldHistory> findLatestByServerName(String serverName) {
        return jpaRepository.findTopByServerNameOrderByDateSendDescIdDesc(serverName);
    }

    @Override
    public Page<HornBugleWorldHistory> findByServerName(String serverName, Pageable pageable) {
        return jpaRepository.findByServerName(serverName, pageable);
    }

    @Override
    public Page<HornBugleWorldHistory> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public Slice<HornBugleWorldHistory> findRecent(Pageable pageable) {
        return jpaRepository.findRecent(pageable);
    }

    @Override
    public Slice<HornBugleWorldHistory> findRecentByServerName(
            String serverName, Pageable pageable) {
        return jpaRepository.findRecentByServerName(serverName, pageable);
    }

    @Override
    public List<HornBugleWorldHistory> findByServerNameAndDateSend(
            String serverName, Instant dateSend) {
        return jpaRepository.findByServerNameAndDateSend(serverName, dateSend);
    }

    @Override
    public Page<HornBugleWorldHistory> searchByKeyword(String keyword, Pageable pageable) {
        return jpaRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<HornBugleWorldHistory> searchByKeywordAndServerName(
            String keyword, String serverName, Pageable pageable) {
        return jpaRepository.searchByKeywordAndServerName(keyword, serverName, pageable);
    }
}
