package until.the.eternity.auctionhistory.application.service.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
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

        while (true) {
            var response = client.fetchAuctionHistory(category, cursor).block();

            if (response == null || response.auctionHistory() == null) {
                log.warn(
                        "> [SCHEDULE] [{}] response or its history is null, something is wrong with open api call",
                        category.getSubCategory());
                break;
            }

            log.debug(
                    "> [SCHEDULE] [{}] fetched '{}' data",
                    category.getSubCategory(),
                    response.auctionHistory().size());

            if (response.auctionHistory().isEmpty()) {
                log.debug("> [SCHEDULE] [{}] fetched no data", category.getSubCategory());
                break;
            }

            var batch = response.auctionHistory();

            OptionalInt duplicateIndex = duplicateChecker.checkDuplicateInBatch(batch, category);

            if (duplicateIndex.isPresent()) {
                int index = duplicateIndex.getAsInt();
                if (index > 0) {
                    result.addAll(batch.subList(0, index));
                }
                log.debug(
                        "> [SCHEDULE] [{}] duplicate found at index {}, added {} items before duplicate",
                        category.getSubCategory(),
                        index,
                        index);
                break;
            }

            result.addAll(batch);

            cursor = response.nextCursor();

            if (cursor == null || cursor.isEmpty()) {
                log.debug(
                        "> [SCHEDULE] [{}] response cursor is null, fetched end",
                        category.getSubCategory());
                break;
            }
        }

        return result;
    }
}
