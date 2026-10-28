package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;

@Schema(description = "숙련도 검색 조건")
public record ProficiencySearchRequest(
        @Schema(description = "숙련도 값", example = "15") Integer proficiency,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하, EQUAL: 같음)", example = "UP")
                SearchStandard proficiencyStandard) {}
