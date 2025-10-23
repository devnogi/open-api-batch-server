package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 내구력 검색 조건")
public record MaximumDurabilitySearchRequest(
        @Schema(description = "최대 내구력 값", example = "20") Integer maximumDurability,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String maximumDurabilityStandard) {}
