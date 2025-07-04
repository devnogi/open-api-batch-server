package until.the.eternity.itemminprice.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
        name = "item_daily_min_price",
        indexes = {
            @Index(
                    name = "idx_item_daily_min_price_item_name_date_auction_buy",
                    columnList = "item_name, created_date")
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
    @Schema(description = "기록된 최저 단가 (거래내역이 없으면 레코드 자체를 생성하지 않음)", example = "120000")
    private Long minPrice;

    @Column(name = "date_auction_buy", nullable = false)
    @Schema(description = "거래 일자 (해당 데이터가 저장된 일시보다 9시간 전 일자)", example = "2025-07-01")
    private LocalDate dateAuctionBuy;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "해당 데이터가 저장된 일시", example = "2025-07-01T14:35:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "해당 데이터가 수정된 일시", example = "2025-07-01T15:35:00")
    private LocalDateTime updatedAt;
}
