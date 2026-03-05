package until.the.eternity.auctionsearchoption.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.FieldMetadata;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;
import until.the.eternity.config.CacheNames;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSearchOptionService {

    private final AuctionSearchOptionRepositoryPort repositoryPort;
    private final ObjectMapper objectMapper;

    /**
     * 모든 활성화된 검색 옵션 조회
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    @Cacheable(
            cacheNames = CacheNames.SEARCH_OPTION_ALL_ACTIVE,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).all()")
    @Transactional(readOnly = true)
    public List<SearchOptionMetadataResponse> getAllActiveSearchOptions() {
        List<AuctionSearchOptionMetadata> entities = repositoryPort.findAllActive();

        return entities.stream().map(this::toResponse).toList();
    }

    private SearchOptionMetadataResponse toResponse(AuctionSearchOptionMetadata entity) {
        Map<String, FieldMetadata> searchCondition =
                parseJsonToFieldMetadata(entity.getSearchConditionJson());

        return new SearchOptionMetadataResponse(
                entity.getId(),
                entity.getSearchOptionName(),
                searchCondition,
                entity.getDisplayOrder());
    }

    private Map<String, FieldMetadata> parseJsonToFieldMetadata(String json) {
        try {
            TypeReference<Map<String, FieldMetadata>> typeRef = new TypeReference<>() {};
            return objectMapper.readValue(json, typeRef);
        } catch (Exception e) {
            log.error("Failed to parse JSON to FieldMetadata: {}", json, e);
            throw new IllegalStateException("JSON 파싱 실패", e);
        }
    }
}
