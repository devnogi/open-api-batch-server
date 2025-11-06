package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;

@Schema(description = "마법 보호 검색 조건")
public record MagicProtectSearchRequest(
        @Schema(description = "마법 보호 값", example = "2") Integer magicProtect,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하, EQUAL: 같음)", example = "UP")
                SearchStandard magicProtectStandard) {}
