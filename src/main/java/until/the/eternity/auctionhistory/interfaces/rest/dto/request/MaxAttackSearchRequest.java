package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 공격력 검색 조건 (범위)")
public record MaxAttackSearchRequest(
        @Schema(description = "최대 공격력 최소값", example = "50") Integer maxAttackFrom,
        @Schema(description = "최대 공격력 최대값", example = "100") Integer maxAttackTo) {}
