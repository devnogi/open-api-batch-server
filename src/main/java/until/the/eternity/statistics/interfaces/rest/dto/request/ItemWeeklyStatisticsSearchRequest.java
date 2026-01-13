package until.the.eternity.statistics.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "아이템별 주간 통계 검색 요청")
public record ItemWeeklyStatisticsSearchRequest(
        @NotBlank(message = "아이템 이름은 필수입니다")
                @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드", required = true)
                String itemName,
        @NotBlank(message = "서브 카테고리는 필수입니다")
                @Schema(description = "아이템 서브 카테고리", example = "한손검", required = true)
                String subCategory,
        @NotBlank(message = "탑 카테고리는 필수입니다")
                @Schema(description = "아이템 탑 카테고리", example = "무기", required = true)
                String topCategory,
        @NotNull(message = "시작 일자는 필수입니다")
                @Schema(description = "검색 시작 일자", example = "2025-01-01", required = true)
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate startDate,
        @NotNull(message = "종료 일자는 필수입니다")
                @Schema(description = "검색 종료 일자", example = "2025-04-30", required = true)
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate endDate) {}
