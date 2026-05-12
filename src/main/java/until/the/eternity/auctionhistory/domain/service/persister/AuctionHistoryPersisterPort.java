package until.the.eternity.auctionhistory.domain.service.persister;

import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.util.List;

public interface AuctionHistoryPersisterPort {

    List<AuctionHistory> filterOutExisting(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category);
}
