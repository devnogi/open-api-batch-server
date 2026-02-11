package until.the.eternity.metalwareinfo.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.metalwareinfo.infrastructure.persistence.MetalwareAttributeInfoEntity;

public interface MetalwareAttributeInfoRepositoryPort {

    int syncFromAuctionHistory();

    Page<MetalwareAttributeInfoEntity> searchByMetalware(String metalware, Pageable pageable);
}
