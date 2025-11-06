package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;

@Schema(description = "마법 방어력 검색 조건")
public record MagicDefenseSearchRequest(
        @Schema(description = "마법 방어력 값", example = "3") Integer magicDefense,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하, EQUAL: 같음)", example = "UP")
                SearchStandard magicDefenseStandard) {}
