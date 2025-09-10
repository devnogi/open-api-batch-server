package until.the.eternity.itemminprice.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "ItemDailyMinPriceDto", description = "아이템 일간 최저가 DTO")
public record ItemDailyMinPriceResponseDto(
        @Schema(description = "고유 식별자", example = "1") Long id,
        @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드") String itemName,
        @Schema(description = "기록된 최저 단가", example = "120000") Long minPrice,
        @Schema(description = "해당 가격이 발견된 시각 (거래 발생 시각)", example = "2025-07-01T14:35:00")
                LocalDateTime dateAuctionBuy,
        @Schema(description = "데이터가 저장된 일자", example = "2025-07-01") LocalDate createdAt) {}
