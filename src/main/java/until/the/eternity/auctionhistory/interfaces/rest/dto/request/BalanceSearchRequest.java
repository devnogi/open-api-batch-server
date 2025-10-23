package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "밸런스 검색 조건")
public record BalanceSearchRequest(
        @Schema(description = "밸런스 값", example = "10") Integer balance,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP") String balanceStandard) {}
