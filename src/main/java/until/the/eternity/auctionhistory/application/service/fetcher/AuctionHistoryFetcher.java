package until.the.eternity.auctionhistory.application.service.fetcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.domain.service.fetcher.AuctionHistoryFetcherPort;
import until.the.eternity.auctionhistory.infrastructure.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcher implements AuctionHistoryFetcherPort {

    private final AuctionHistoryClient client;
    private final AuctionHistoryDuplicateChecker duplicateChecker;

    @Override
    public List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category) {

        List<OpenApiAuctionHistoryResponse> result = new ArrayList<>();
        String cursor = null;

        do {
            var response = client.fetchAuctionHistory(category, cursor);
            if (response == null || response.auctionHistory() == null) break;

            var batch = response.auctionHistory();
            if (duplicateChecker.hasDuplicate(batch)) break;

            result.addAll(batch);
            cursor = response.nextCursor();
        } while (cursor != null);

        log.debug("[{}] fetched {} entries", category.getSubCategory(), result.size());
        return result;
    }
}
