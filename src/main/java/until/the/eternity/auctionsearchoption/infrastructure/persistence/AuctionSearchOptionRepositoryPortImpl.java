package until.the.eternity.auctionsearchoption.infrastructure.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;

@Component
@RequiredArgsConstructor
class AuctionSearchOptionRepositoryPortImpl implements AuctionSearchOptionRepositoryPort {

    private final AuctionSearchOptionJpaRepository jpaRepository;

    @Override
    public List<AuctionSearchOptionMetadata> findAllActive() {
        return jpaRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    @Override
    public List<AuctionSearchOptionMetadata> findAll() {
        return jpaRepository.findAllByOrderByDisplayOrderAsc();
    }
}
