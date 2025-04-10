package until.the.eternity.auction.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class AuctionHistoryResponse {
    @JsonProperty("auction_history")
    private List<AuctionHistoryDto> auction_history;

    @JsonProperty("next_cursor")
    private String next_cursor;
}
