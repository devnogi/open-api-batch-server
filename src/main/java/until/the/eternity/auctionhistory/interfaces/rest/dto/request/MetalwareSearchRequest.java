package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세공 검색 조건")
public record MetalwareSearchRequest(
        @Schema(description = "세공 이름 (완전 일치)", example = "불의 연성술") String metalware,
        @Schema(description = "세공 레벨 시작값 (null이면 1로 처리)", example = "1") Integer levelFrom,
        @Schema(description = "세공 레벨 종료값 (null이면 30으로 처리)", example = "30") Integer levelTo) {

    public int resolvedLevelFrom() {
        return levelFrom != null ? levelFrom : 1;
    }

    public int resolvedLevelTo() {
        return levelTo != null ? levelTo : 30;
    }
}
