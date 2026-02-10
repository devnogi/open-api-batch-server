package until.the.eternity.statistics.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "탑카테고리별 일간 통계 검색 요청")
public record TopCategoryDailyStatisticsSearchRequest(
        @NotBlank(message = "탑 카테고리는 필수입니다")
                @Schema(description = "아이템 탑 카테고리", example = "소모품", required = true)
                String topCategory,
        @Schema(description = "검색 시작 일자 (미입력 시 오늘로부터 2주 전)", example = "2026-01-01")
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate startDate,
        @Schema(description = "검색 종료 일자 (미입력 시 오늘)", example = "2026-01-31")
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate endDate) {

    public LocalDate getStartDateWithDefault() {
        return startDate != null ? startDate : LocalDate.now().minusDays(14);
    }

    public LocalDate getEndDateWithDefault() {
        return endDate != null ? endDate : LocalDate.now();
    }
}
