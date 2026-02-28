package until.the.eternity.auctionhistory.application.service;

import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
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
import until.the.eternity.config.CacheNames;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepositoryPort repository;
    private final AuctionHistoryMapper mapper;
    private final EntityManager entityManager;

    /**
     * 경매 거래 내역을 검색한다.
     *
     * <p>단순 검색(가격/옵션/인챈트/세공 필터 없음)에 한해 캐싱을 적용한다. TTL 2시간, 배치 완료 시 전체 무효화 + 워밍업.
     */
    @Cacheable(
            cacheNames = CacheNames.AUCTION_HISTORY_SEARCH,
            key =
                    "#pageRequestDto.page() + ':'"
                            + " + #pageRequestDto.size() + ':'"
                            + " + (#pageRequestDto.sortBy() != null ? #pageRequestDto.sortBy().fieldName : 'dateAuctionBuy') + ':'"
                            + " + (#pageRequestDto.direction() != null ? #pageRequestDto.direction().code : 'DESC') + ':'"
                            + " + (#requestDto.itemName() ?: '') + ':'"
                            + " + (#requestDto.itemTopCategory() ?: '') + ':'"
                            + " + (#requestDto.itemSubCategory() ?: '')",
            condition =
                    "#requestDto.itemOptionSearchRequest() == null"
                            + " and #requestDto.enchantSearchRequest() == null"
                            + " and (#requestDto.metalwareSearchRequests() == null or #requestDto.metalwareSearchRequests().isEmpty())"
                            + " and #requestDto.priceSearchRequest() == null")
    @Transactional(readOnly = true)
    public PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> search(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {

        Page<AuctionHistory> page = repository.search(requestDto, pageRequestDto.toPageable());
        Page<AuctionHistoryDetailResponse<ItemOptionResponse>> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
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
