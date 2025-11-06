package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;

@Schema(description = "남은 거래 횟수 검색 조건")
public record RemainingTransactionCountSearchRequest(
        @Schema(description = "남은 거래 횟수", example = "5") Integer remainingTransactionCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하, EQUAL: 같음)", example = "UP")
                SearchStandard remainingTransactionCountStandard) {}
