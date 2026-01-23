package until.the.eternity.auctionsearchoption.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;

import java.util.List;

@Repository
interface AuctionSearchOptionJpaRepository
        extends JpaRepository<AuctionSearchOptionMetadata, Long> {

    List<AuctionSearchOptionMetadata> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<AuctionSearchOptionMetadata> findAllByOrderByDisplayOrderAsc();
}
