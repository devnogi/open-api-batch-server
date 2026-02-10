package until.the.eternity.ranking.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "가격 랭킹 응답")
public record PriceRankingResponse(
        @Schema(description = "순위", example = "1") Integer rank,
        @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드") String itemName,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "아이템 서브 카테고리", example = "한손검") String itemSubCategory,
        @Schema(description = "최고 단가", example = "150000000") Long maxPrice,
        @Schema(description = "평균 단가", example = "135000000.50") BigDecimal avgPrice,
        @Schema(description = "거래 총량 (총 거래 금액)", example = "5000000000") Long totalVolume,
        @Schema(description = "거래 수량", example = "150") Long totalQuantity,
        @Schema(description = "거래 일자", example = "2025-07-01") @JsonFormat(pattern = "yyyy-MM-dd")
                LocalDate dateAuctionBuy) {}
