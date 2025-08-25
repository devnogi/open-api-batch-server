package until.the.eternity.auctionhistory.application.service.fetcher;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.domain.service.fetcher.AuctionHistoryFetcherPort;
import until.the.eternity.auctionhistory.infrastructure.client.AuctionHistoryClient;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcher implements AuctionHistoryFetcherPort {

    private final AuctionHistoryClient client;
    private final AuctionHistoryDuplicateChecker duplicateChecker;

    @Override
    public List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category) {

        List<OpenApiAuctionHistoryResponse> result = new ArrayList<>();
        String cursor = "";

        while(true) {
            var response = client.fetchAuctionHistory(category, cursor);
            log.debug(
                    "> [SCHEDULE] [{}] fetched '{}' data",
                    category.getSubCategory(),
                    response.auctionHistory().size());

            if (response.auctionHistory().isEmpty()) {
                log.debug("> [SCHEDULE] [{}] fetched no data", category.getSubCategory());
                break;
            }

            var batch = response.auctionHistory();
            result.addAll(batch);

            if (duplicateChecker.hasDuplicate(batch.getLast())) {
                log.debug(
                        "> [SCHEDULE] [{}] this fetched data has duplicate data, skip to next item subcategory",
                        category.getSubCategory());
                break;
            }

            cursor = response.nextCursor();

            if (cursor == null || cursor.isEmpty()) {
                log.debug("> [SCHEDULE] [{}] response cursor is null, fetched end", category.getSubCategory());
                break;
            }
        }

        return result;
    }
}
