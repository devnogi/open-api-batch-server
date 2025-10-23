package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "크리티컬 검색 조건")
public record CriticalSearchRequest(
        @Schema(description = "크리티컬 값", example = "30") Integer critical,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String criticalStandard) {}
