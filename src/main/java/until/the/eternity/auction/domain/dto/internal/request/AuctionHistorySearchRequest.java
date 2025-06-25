package until.the.eternity.auction.domain.dto.internal.request;

import lombok.Data;

@Data
public class AuctionHistorySearchRequest {
    private String itemName;
    private String itemTopCategory;
    private String itemSubCategory;
}
