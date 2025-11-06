package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최대 부상률 검색 조건 (범위)")
public record MaxInjuryRateSearchRequest(
        @Schema(description = "최대 부상률 최소값", example = "10") Integer maxInjuryRateFrom,
        @Schema(description = "최대 부상률 최대값", example = "30") Integer maxInjuryRateTo) {}
