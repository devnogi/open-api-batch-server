package until.the.eternity.metalwareinfo.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.common.annotation.BatchLog;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareAttributeInfoRepositoryPort;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.request.MetalwareAttributeInfoSearchRequest;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareAttributeInfoResponse;

@Service
@RequiredArgsConstructor
public class MetalwareAttributeInfoService {

    private final MetalwareAttributeInfoRepositoryPort metalwareAttributeInfoRepository;

    @BatchLog(type = BatchType.METALWARE_ATTRIBUTE_SYNC)
    @Transactional
    public int sync() {
        long before = metalwareAttributeInfoRepository.count();
        int raw = metalwareAttributeInfoRepository.syncFromAuctionHistory();
        long after = metalwareAttributeInfoRepository.count();
        int inserted = (int) (after - before);
        int updated = (raw - inserted) / 2;
        return inserted + updated;
    }

    @Transactional(readOnly = true)
    public Page<MetalwareAttributeInfoResponse> search(
            MetalwareAttributeInfoSearchRequest request) {
        return metalwareAttributeInfoRepository
                .searchByMetalware(request.metalware(), request.toPageable())
                .map(MetalwareAttributeInfoResponse::from);
    }
}
