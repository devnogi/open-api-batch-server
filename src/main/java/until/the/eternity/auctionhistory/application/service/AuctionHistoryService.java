package until.the.eternity.auctionhistory.application.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.AuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.common.util.CacheKeyBuilder;
import until.the.eternity.config.CacheNames;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepositoryPort repository;
    private final AuctionHistoryMapper mapper;
    private final EntityManager entityManager;
    private final CacheManager cacheManager;

    /**
     * 경매 거래 내역을 검색한다.
     *
     * <p>단순 검색(가격/옵션/인챈트/세공 필터 없음)에 한해 캐싱을 적용한다. TTL 2시간, 배치 완료 시 전체 무효화 + 워밍업.
     */
    @Cacheable(
            cacheNames = CacheNames.AUCTION_HISTORY_SEARCH,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildAuctionHistorySearchKey(#requestDto, #pageRequestDto)",
            condition =
                    "#requestDto.itemOptionSearchRequest() == null"
                            + " and #requestDto.enchantSearchRequest() == null"
                            + " and (#requestDto.metalwareSearchRequests() == null or #requestDto.metalwareSearchRequests().isEmpty())"
                            + " and #requestDto.priceSearchRequest() == null",
            sync = true)
    @Transactional(readOnly = true)
    public PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> search(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {

        Pageable pageable = pageRequestDto.toPageable();
        List<AuctionHistory> content = repository.searchContent(requestDto, pageable);
        long total = resolveTotalCount(requestDto);
        Page<AuctionHistory> page = new PageImpl<>(content, pageable, total);
        Page<AuctionHistoryDetailResponse<ItemOptionResponse>> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    private long resolveTotalCount(AuctionHistorySearchRequest requestDto) {
        if (!isCountCacheEligible(requestDto)) {
            return repository.count(requestDto);
        }

        Cache cache = cacheManager.getCache(CacheNames.AUCTION_HISTORY_COUNT);
        if (cache == null) {
            return repository.count(requestDto);
        }

        String cacheKey = CacheKeyBuilder.buildAuctionHistoryCountKey(requestDto);
        Long cachedCount = cache.get(cacheKey, Long.class);
        if (cachedCount != null) {
            return cachedCount;
        }

        long total = repository.count(requestDto);
        cache.put(cacheKey, total);
        return total;
    }

    private boolean isCountCacheEligible(AuctionHistorySearchRequest requestDto) {
        return requestDto.itemOptionSearchRequest() == null
                && requestDto.enchantSearchRequest() == null
                && (requestDto.metalwareSearchRequests() == null
                        || requestDto.metalwareSearchRequests().isEmpty())
                && requestDto.priceSearchRequest() == null;
    }

    @Transactional(readOnly = true)
    public AuctionHistoryDetailResponse<ItemOptionResponse> findByIdOrElseThrow(String id) {
        AuctionHistory auctionHistory =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "AuctionHistory not found: " + id));
        return mapper.toDto(auctionHistory);
    }

    @Transactional
    public void saveAll(List<AuctionHistory> entities) {
        repository.saveAll(entities);
        entityManager.flush();
        entityManager.clear();
    }
}
