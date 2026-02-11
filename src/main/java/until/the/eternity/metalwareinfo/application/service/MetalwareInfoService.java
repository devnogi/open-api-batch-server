package until.the.eternity.metalwareinfo.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareInfoRepositoryPort;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoResponse;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoSyncResponse;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MetalwareInfoService {

    private final MetalwareInfoRepositoryPort metalwareInfoRepository;

    public List<MetalwareInfoResponse> findAll() {
        List<String> metalwares = metalwareInfoRepository.findAllMetalwares();
        return MetalwareInfoResponse.from(metalwares);
    }

    @Transactional
    public MetalwareInfoSyncResponse syncFromAttributeInfo() {
        int levelAttributeUpserted =
                metalwareInfoRepository.upsertLevelAttributeFromAttributeInfo();
        int limitBreakLevelUpserted =
                metalwareInfoRepository.upsertLimitBreakLevelFromAttributeInfo();

        return MetalwareInfoSyncResponse.builder()
                .levelAttributeUpsertedCount(levelAttributeUpserted)
                .limitBreakLevelUpsertedCount(limitBreakLevelUpserted)
                .totalUpsertedCount(levelAttributeUpserted + limitBreakLevelUpserted)
                .build();
    }
}
