package until.the.eternity.statistics.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "탑카테고리별 주간 통계 응답")
public record TopCategoryWeeklyStatisticsResponse(
        @Schema(description = "고유 식별자", example = "1") Long id,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "연도", example = "2025") Integer year,
        @Schema(description = "주차 번호", example = "27") Integer weekNumber,
        @Schema(description = "주 시작일 (월요일)", example = "2025-07-01")
                @JsonFormat(pattern = "yyyy-MM-dd")
                LocalDate weekStartDate,
        @Schema(description = "최저 단가 (해당 주의 모든 거래 중 최저)", example = "120000") Long minPrice,
        @Schema(description = "최고 단가 (해당 주의 모든 거래 중 최고)", example = "150000") Long maxPrice,
        @Schema(description = "평균 단가 (Daily 평균가의 평균)", example = "135000.50") BigDecimal avgPrice,
        @Schema(description = "거래 총량 (총 거래 금액)", example = "3500000000") Long totalVolume,
        @Schema(description = "거래 수량 (itemCount 합계)", example = "105000") Long totalQuantity,
        @Schema(description = "생성 일시", example = "2025-07-08T14:35:00")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime createdAt,
        @Schema(description = "수정 일시", example = "2025-07-08T15:35:00")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
                LocalDateTime updatedAt) {}
