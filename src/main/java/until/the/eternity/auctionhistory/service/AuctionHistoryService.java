package until.the.eternity.auctionhistory.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.component.AuctionHistoryFetcher;
import until.the.eternity.auctionhistory.domain.component.AuctionHistoryPersister;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.domain.dto.internal.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.domain.dto.internal.response.ItemOptionResponse;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.AuctionHistoryMapper;
import until.the.eternity.auctionhistory.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepository repository;
    private final AuctionHistoryFetcher fetcher;
    private final AuctionHistoryPersister persister;
    private final AuctionHistoryMapper mapper;

    @Value("${openapi.auction-history.delay-ms}")
    private long delayMs;

    @Scheduled(cron = "0 0 * * * *")
    public void fetchAndSaveAuctionHistoryAll() {
        for (ItemCategory category : ItemCategory.values()) {
            try {
                fetchAndSaveAuctionHistory(category);
            } catch (Exception e) {
                log.error("Error during processing category [{}]", category.getSubCategory(), e);
            }
            delayBetweenRequests();
        }
    }

    private void fetchAndSaveAuctionHistory(ItemCategory category) {
        List<OpenApiAuctionHistoryResponse> dtoList = fetcher.fetch(category);

        if (dtoList == null || dtoList.isEmpty()) {
            log.info("[{}] No auction history data received", category.getSubCategory());
            return;
        }

        persister.saveIfNotExists(dtoList, category);
    }

    private void delayBetweenRequests() {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during delay between requests", e);
        }
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> search(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {

        // 조건 검색 + 페이징
        Page<AuctionHistory> page = repository.search(requestDto, pageRequestDto.toPageable());

        // Entity → DTO 변환
        Page<AuctionHistoryDetailResponse<ItemOptionResponse>> dtoPage = page.map(mapper::toDto);

        // PageResponseDto로 래핑해 반환
        return PageResponseDto.of(dtoPage);
    }

    public AuctionHistory findByIdOrElseThrow(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuctionHistory not found: " + id));
    }
}
