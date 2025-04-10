package until.the.eternity.auction.domain.model;

import lombok.Data;

@Data
public class AuctionHistorySearchDto {
    private String category; // sub-category
    private String itemName;
}
