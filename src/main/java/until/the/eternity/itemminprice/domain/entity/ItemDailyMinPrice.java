package until.the.eternity.itemminprice.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "item_daily_min_price",
        indexes = {
            @Index(
                    name = "idx_item_daily_min_price_item_name_week_start_date",
                    columnList = "item_name, created_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "아이템별 일간 최저가 이력")
public class ItemDailyMinPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "고유 식별자", example = "1")
    private Long id;

    @Column(name = "item_name", nullable = false, length = 255)
    @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드")
    private String itemName;

    @Column(name = "min_price", nullable = false)
    @Schema(description = "기록된 최저 단가", example = "120000")
    private Long minPrice;

    @Column(name = "date_auction_buy", nullable = false)
    @Schema(description = "해당 가격이 발견된 시각 (거래 발생 시각)", example = "2025-07-01T14:35:00")
    private LocalDateTime dateAuctionBuy;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "해당 데이터가 저장된 일자", example = "2025-07-01")
    private LocalDate createdAt;
}
