package until.the.eternity.auctionhistory.domain.service.fetcher;

import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.List;

public interface AuctionHistoryFetcherPort {
    List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category);
}
