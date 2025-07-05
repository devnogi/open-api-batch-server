package until.the.eternity.auctionhistory.domain.component;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcherImpl {

    private final AuctionHistoryClient auctionHistoryClient;
    private final AuctionHistoryDuplicateChecker duplicateChecker;

    public List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category) {

        List<OpenApiAuctionHistoryResponse> result = new ArrayList<>();
        String cursor = null;

        do {
            var response = auctionHistoryClient.fetchAuctionHistory(category, cursor);
            if (response == null || response.auctionHistory() == null) break;

            var batch = response.auctionHistory();
            // 기존 데이터와 ID 중복 시 fetch 중단
            if (duplicateChecker.hasDuplicate(batch)) break;

            result.addAll(batch);
            cursor = response.nextCursor();
        } while (cursor != null); // 커서가 null 응답이 오면 fetch 중단

        log.debug("[{}] fetched {} entries", category.getSubCategory(), result.size());
        return result;
    }
}
