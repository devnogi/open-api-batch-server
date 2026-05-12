package until.the.eternity.metalwareinfo.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareInfoRepositoryPort;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoResponse;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoSyncResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MetalwareInfoService {

    private final MetalwareInfoRepositoryPort metalwareInfoRepository;

    @Cacheable(
            cacheNames = CacheNames.METALWARE_INFO_ALL,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).all()",
            sync = true)
    public List<MetalwareInfoResponse> findAll() {
        List<String> metalwares = metalwareInfoRepository.findAllMetalwares();
        return MetalwareInfoResponse.from(metalwares);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = CacheNames.METALWARE_INFO_ALL, allEntries = true),
                @CacheEvict(
                        cacheNames = CacheNames.METALWARE_ATTRIBUTE_INFO_SEARCH,
                        allEntries = true)
            })
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
