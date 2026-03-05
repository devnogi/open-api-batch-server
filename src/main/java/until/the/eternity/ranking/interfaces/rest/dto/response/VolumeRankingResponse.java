package until.the.eternity.ranking.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "거래량 랭킹 응답")
public record VolumeRankingResponse(
        @Schema(description = "순위", example = "1") Integer rank,
        @Schema(description = "아이템 이름", example = "축복의 포션") String itemName,
        @Schema(description = "아이템 탑 카테고리", example = "소모품") String itemTopCategory,
        @Schema(description = "아이템 서브 카테고리", example = "포션") String itemSubCategory,
        @Schema(description = "거래 수량", example = "15000") Long totalQuantity,
        @Schema(description = "거래 총량 (총 거래 금액)", example = "500000000") Long totalVolume,
        @Schema(description = "평균 단가", example = "33333.33") BigDecimal avgPrice,
        @Schema(description = "거래 일자", example = "2025-07-01") @JsonFormat(pattern = "yyyy-MM-dd")
                LocalDate dateAuctionBuy) {}
