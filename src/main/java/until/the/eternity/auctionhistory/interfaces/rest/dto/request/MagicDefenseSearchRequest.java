package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마법 방어력 검색 조건")
public record MagicDefenseSearchRequest(
        @Schema(description = "마법 방어력 값", example = "3") Integer magicDefense,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String magicDefenseStandard) {}
