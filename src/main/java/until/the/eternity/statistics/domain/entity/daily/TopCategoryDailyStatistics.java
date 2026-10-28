package until.the.eternity.statistics.domain.entity.daily;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
        name = "top_category_daily_statistics",
        indexes = {
            @Index(
                    name = "idx_top_category_daily_statistics_category_date",
                    columnList = "item_top_category, date_auction_buy")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_top_category_daily_statistics_category_date",
                    columnNames = {"item_top_category", "date_auction_buy"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "탑카테고리별 일간 통계")
public class TopCategoryDailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "고유 식별자", example = "1")
    private Long id;

    @Column(name = "item_top_category", nullable = false, length = 255)
    @Schema(description = "아이템 탑 카테고리", example = "무기")
    private String itemTopCategory;

    @Column(name = "date_auction_buy", nullable = false)
    @Schema(description = "거래 일자", example = "2025-07-01")
    private LocalDate dateAuctionBuy;

    @Column(name = "min_price", nullable = false)
    @Schema(description = "최저 단가", example = "120000")
    private Long minPrice;

    @Column(name = "max_price", nullable = false)
    @Schema(description = "최고 단가", example = "150000")
    private Long maxPrice;

    @Column(name = "avg_price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "평균 단가", example = "135000.50")
    private BigDecimal avgPrice;

    @Column(name = "total_volume", nullable = false)
    @Schema(description = "거래 총량 (총 거래 금액)", example = "500000000")
    private Long totalVolume;

    @Column(name = "total_quantity", nullable = false)
    @Schema(description = "거래 수량 (itemCount 합계)", example = "15000")
    private Long totalQuantity;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "생성 일시", example = "2025-07-01T14:35:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "수정 일시", example = "2025-07-01T15:35:00")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
