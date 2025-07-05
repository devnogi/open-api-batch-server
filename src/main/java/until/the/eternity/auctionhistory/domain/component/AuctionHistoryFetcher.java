package until.the.eternity.auctionhistory.domain.component;

import java.util.List;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

public interface AuctionHistoryFetcher {
    List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category);
}
