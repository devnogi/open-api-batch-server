package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에르그 검색 조건 (범위)")
public record ErgSearchRequest(
        @Schema(description = "에르그 최소값", example = "10") Integer ergFrom,
        @Schema(description = "에르그 최대값", example = "50") Integer ergTo) {}
