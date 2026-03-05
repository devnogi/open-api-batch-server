package until.the.eternity.ranking.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "가격 변동 랭킹 응답")
public record PriceChangeRankingResponse(
        @Schema(description = "순위", example = "1") Integer rank,
        @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드") String itemName,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "아이템 서브 카테고리", example = "한손검") String itemSubCategory,
        @Schema(description = "오늘 평균 단가", example = "150000000.00") BigDecimal todayAvgPrice,
        @Schema(description = "어제 평균 단가", example = "100000000.00") BigDecimal yesterdayAvgPrice,
        @Schema(description = "변동률 (%)", example = "50.00") BigDecimal changeRate,
        @Schema(description = "변동액", example = "50000000") Long priceChange) {}
