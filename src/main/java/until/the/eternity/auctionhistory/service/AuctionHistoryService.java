package until.the.eternity.auctionhistory.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.component.AuctionHistoryFetcherImpl;
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
    private final AuctionHistoryFetcherImpl fetcher;
    private final AuctionHistoryPersister persister;
    private final AuctionHistoryMapper mapper;

    @Transactional(readOnly = true)
    public PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> search(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {

        Page<AuctionHistory> page = repository.search(requestDto, pageRequestDto.toPageable());
        Page<AuctionHistoryDetailResponse<ItemOptionResponse>> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    public AuctionHistory findByIdOrElseThrow(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AuctionHistory not found: " + id));
    }

    /** 하나의 카테고리에 대해 API 데이터를 fetch & 저장하는 로직 */
    @Transactional
    public void fetchAndSaveAuctionHistory(ItemCategory category) {
        List<OpenApiAuctionHistoryResponse> dtoList = fetcher.fetch(category);

        if (dtoList == null || dtoList.isEmpty()) {
            log.info("[{}] No auction history data received", category.getSubCategory());
            return;
        }

        persister.saveIfNotExists(dtoList, category);
    }
}
