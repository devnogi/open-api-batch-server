package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보호 검색 조건")
public record ProtectSearchRequest(
        @Schema(description = "보호 값", example = "1") Integer protect,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String protectStandard) {}
