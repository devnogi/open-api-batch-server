package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "남은 사용 횟수 검색 조건")
public record RemainingUseCountSearchRequest(
        @Schema(description = "남은 사용 횟수", example = "10") Integer remainingUseCount,
        @Schema(description = "검색 기준 (UP: 이상, DOWN: 이하)", example = "DOWN")
                String remainingUseCountStandard) {}
