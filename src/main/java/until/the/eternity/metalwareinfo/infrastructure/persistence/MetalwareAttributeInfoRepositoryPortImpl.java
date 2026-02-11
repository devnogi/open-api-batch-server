package until.the.eternity.metalwareinfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareAttributeInfoRepositoryPort;

@Repository
@RequiredArgsConstructor
public class MetalwareAttributeInfoRepositoryPortImpl
        implements MetalwareAttributeInfoRepositoryPort {

    private final MetalwareAttributeInfoJpaRepository jpaRepository;

    @Override
    public int syncFromAuctionHistory() {
        return jpaRepository.syncFromAuctionHistory();
    }

    @Override
    public Page<MetalwareAttributeInfoEntity> searchByMetalware(
            String metalware, Pageable pageable) {
        return jpaRepository.findByMetalwareContaining(metalware, pageable);
    }
}
