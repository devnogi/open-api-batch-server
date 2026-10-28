package until.the.eternity.ranking.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "거래량 변동 랭킹 응답")
public record VolumeChangeRankingResponse(
        @Schema(description = "순위", example = "1") Integer rank,
        @Schema(description = "아이템 이름", example = "축복의 포션") String itemName,
        @Schema(description = "아이템 탑 카테고리", example = "소모품") String itemTopCategory,
        @Schema(description = "아이템 서브 카테고리", example = "포션") String itemSubCategory,
        @Schema(description = "오늘 거래 수량", example = "15000") Long todayQuantity,
        @Schema(description = "어제 거래 수량", example = "5000") Long yesterdayQuantity,
        @Schema(description = "변동률 (%)", example = "200.00") BigDecimal changeRate,
        @Schema(description = "변동량", example = "10000") Long quantityChange) {}
