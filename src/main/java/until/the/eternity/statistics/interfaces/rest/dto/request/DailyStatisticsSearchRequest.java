package until.the.eternity.statistics.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Schema(description = "일간 통계 검색 요청")
public record DailyStatisticsSearchRequest(
        @Schema(description = "아이템 이름 (부분 일치)", example = "켈틱") String itemName,
        @Schema(description = "아이템 서브 카테고리", example = "한손검") String itemSubCategory,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "거래 시작 일자", example = "2025-07-01")
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate dateFrom,
        @Schema(description = "거래 종료 일자", example = "2025-07-31")
                @DateTimeFormat(pattern = "yyyy-MM-dd")
                LocalDate dateTo) {}
