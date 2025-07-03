package until.the.eternity.auctionhistory.domain.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryListResponse;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auctionhistory.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 외부 경매 히스토리를 수집한 뒤 DB에 존재하지 않는 범위까지만 반환한다.
 *
 * <p>API 호출 · 재시도 로직은 {@link AuctionHistoryClient} 로 위임하여 SRP와 DIP를 지킴.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcher {

    private final AuctionHistoryClient auctionHistoryClient;
    private final AuctionHistoryRepository auctionHistoryRepository;

    /** 외부 OPEN API에서 경매 히스토리를 가져온 뒤, 이미 저장된 ID가 나오면 중단 */
    public List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category) {
        List<OpenApiAuctionHistoryResponse> result = new ArrayList<>();
        String cursor = null;

        do {
            OpenApiAuctionHistoryListResponse response =
                    auctionHistoryClient.fetchAuctionHistory(category, cursor);
            if (response == null || response.auctionHistory() == null) break;

            List<OpenApiAuctionHistoryResponse> currentBatch = response.auctionHistory();
            if (containsExistingIds(currentBatch)) break;

            result.addAll(currentBatch);
            cursor = response.nextCursor();
        } while (cursor != null);

        return result;
    }

    /** 이미 DB에 존재하는 auctionBuyId가 포함돼 있는지 검사 */
    private boolean containsExistingIds(List<OpenApiAuctionHistoryResponse> dtos) {
        List<String> ids =
                dtos.stream()
                        .map(OpenApiAuctionHistoryResponse::auctionBuyId)
                        .collect(Collectors.toList());
        return auctionHistoryRepository.existsByAuctionBuyIdIn(ids);
    }
}
