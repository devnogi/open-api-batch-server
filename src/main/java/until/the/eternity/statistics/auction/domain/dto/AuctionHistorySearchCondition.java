package until.the.eternity.statistics.auction.domain.dto;

import lombok.Data;

@Data
public class AuctionHistorySearchCondition {
    private String itemName;
    private String itemTopCategory;
    private String itemSubCategory;
}
