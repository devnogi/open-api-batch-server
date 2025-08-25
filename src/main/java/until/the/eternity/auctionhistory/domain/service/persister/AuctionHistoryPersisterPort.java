package until.the.eternity.auctionhistory.domain.service.persister;

import java.util.List;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

public interface AuctionHistoryPersisterPort {

    List<AuctionHistory> filterOutExisting(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category);
}
