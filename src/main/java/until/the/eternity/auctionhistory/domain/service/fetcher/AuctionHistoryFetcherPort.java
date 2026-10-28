package until.the.eternity.auctionhistory.domain.service.fetcher;

import java.util.List;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

public interface AuctionHistoryFetcherPort {
    List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category);
}
