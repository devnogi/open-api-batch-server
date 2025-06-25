package until.the.eternity.auction.domain.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class OpenApiAuctionHistoryListResponse {
    @JsonProperty("auction_history")
    private List<OpenApiAuctionHistoryResponse> auction_history;

    @JsonProperty("next_cursor")
    private String next_cursor;
}
