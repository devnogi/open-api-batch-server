package until.the.eternity.statistics.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "탑카테고리별 주간 통계 검색 요청")
public record TopCategoryWeeklyStatisticsSearchRequest(
        @NotBlank(message = "탑 카테고리는 필수입니다")
                @Schema(description = "아이템 탑 카테고리", example = "소모품", required = true)
                String topCategory,
        @NotNull(message = "시작 일자는 필수입니다")
                @Schema(description = "검색 시작 일자", example = "2026-01-01", required = true)
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate startDate,
        @NotNull(message = "종료 일자는 필수입니다")
                @Schema(description = "검색 종료 일자", example = "2026-04-30", required = true)
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate endDate) {}
