package until.the.eternity.ranking.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "역대 기록 랭킹 응답")
public record AllTimeRankingResponse(
        @Schema(description = "순위", example = "1") Integer rank,
        @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드") String itemName,
        @Schema(description = "아이템 표시 이름", example = "켈틱 로열 나이트 소드 (인챈트)") String itemDisplayName,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "아이템 서브 카테고리", example = "한손검") String itemSubCategory,
        @Schema(description = "단가 (개당 가격)", example = "999999999") Long auctionPricePerUnit,
        @Schema(description = "거래 수량", example = "1") Long itemCount,
        @Schema(description = "총 거래 금액", example = "999999999") Long totalPrice,
        @Schema(description = "거래 일시", example = "2025-07-01T14:35:00Z")
                @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
                Instant dateAuctionBuy) {}
