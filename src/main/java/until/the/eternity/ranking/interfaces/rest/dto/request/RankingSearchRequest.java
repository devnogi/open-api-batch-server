package until.the.eternity.ranking.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import until.the.eternity.ranking.util.RankingConstants;

@Schema(description = "랭킹 검색 요청")
public record RankingSearchRequest(
        @Schema(description = "조회할 랭킹 개수 (기본값: 100, 최대: 100)", example = "100")
                @Min(value = 1, message = "limit은 최소 1 이상이어야 합니다")
                @Max(value = 100, message = "limit은 최대 100까지 가능합니다")
                Integer limit) {

    public int getLimitWithDefault() {
        return limit != null ? limit : RankingConstants.DEFAULT_LIMIT;
    }
}
