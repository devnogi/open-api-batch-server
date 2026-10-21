package until.the.eternity.auctionsearchoption.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;

@Repository
interface AuctionSearchOptionJpaRepository
        extends JpaRepository<AuctionSearchOptionMetadata, Long> {

    List<AuctionSearchOptionMetadata> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<AuctionSearchOptionMetadata> findAllByOrderByDisplayOrderAsc();
}
