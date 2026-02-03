package until.the.eternity.ranking.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import until.the.eternity.ranking.util.RankingConstants;

@Schema(description = "카테고리별 랭킹 검색 요청")
public record CategoryRankingSearchRequest(
        @NotBlank(message = "탑 카테고리는 필수입니다")
                @Schema(description = "아이템 탑 카테고리", example = "무기", required = true)
                String topCategory,
        @Schema(description = "아이템 서브 카테고리 (선택)", example = "한손검") String subCategory,
        @Schema(description = "조회할 랭킹 개수 (기본값: 100, 최대: 100)", example = "100")
                @Min(value = 1, message = "limit은 최소 1 이상이어야 합니다")
                @Max(value = 100, message = "limit은 최대 100까지 가능합니다")
                Integer limit) {

    public int getLimitWithDefault() {
        return limit != null ? limit : RankingConstants.DEFAULT_LIMIT;
    }
}
