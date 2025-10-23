package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "남은 전용 해제 가능 횟수 검색 조건")
public record RemainingUnsealCountSearchRequest(
        @Schema(description = "남은 전용 해제 가능 횟수", example = "3") Integer remainingUnsealCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "UP")
                String remainingUnsealCountStandard) {}
