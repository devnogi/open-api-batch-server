package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "방어력 검색 조건")
public record DefenseSearchRequest(
        @Schema(description = "방어력 값", example = "5") Integer defense,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String defenseStandard) {}
