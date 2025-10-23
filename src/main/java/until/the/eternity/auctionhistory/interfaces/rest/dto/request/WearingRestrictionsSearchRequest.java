package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "착용 제한 검색 조건")
public record WearingRestrictionsSearchRequest(
        @Schema(description = "착용 제한", example = "자이언트 전용") String wearingRestrictions) {}
