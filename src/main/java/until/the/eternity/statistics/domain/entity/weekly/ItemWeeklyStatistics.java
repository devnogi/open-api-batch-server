package until.the.eternity.statistics.domain.entity.weekly;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
        name = "item_weekly_statistics",
        indexes = {
            @Index(
                    name = "idx_item_weekly_statistics_item_name_year_week",
                    columnList = "item_name, year, week_number"),
            @Index(
                    name = "idx_item_weekly_item_category_start_date",
                    columnList =
                            "item_name, item_top_category, item_sub_category, week_start_date"),
            @Index(
                    name = "idx_item_weekly_year_week_total_quantity",
                    columnList = "year, week_number, total_quantity DESC")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_item_weekly_statistics_item_name_year_week",
                    columnNames = {"item_name", "year", "week_number"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "아이템별 주간 통계")
public class ItemWeeklyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "고유 식별자", example = "1")
    private Long id;

    @Column(name = "item_name", nullable = false, length = 255)
    @Schema(description = "아이템 이름", example = "켈틱 로열 나이트 소드")
    private String itemName;

    @Column(name = "item_top_category", nullable = false, length = 255)
    @Schema(description = "아이템 탑 카테고리", example = "무기")
    private String itemTopCategory;

    @Column(name = "item_sub_category", nullable = false, length = 255)
    @Schema(description = "아이템 서브 카테고리", example = "한손검")
    private String itemSubCategory;

    @Column(name = "year", nullable = false)
    @Schema(description = "연도", example = "2025")
    private Integer year;

    @Column(name = "week_number", nullable = false)
    @Schema(description = "주차 번호", example = "27")
    private Integer weekNumber;

    @Column(name = "week_start_date", nullable = false)
    @Schema(description = "주 시작일 (월요일)", example = "2025-07-01")
    private LocalDate weekStartDate;

    @Column(name = "min_price", nullable = false)
    @Schema(description = "최저 단가 (해당 주의 모든 거래 중 최저)", example = "120000")
    private Long minPrice;

    @Column(name = "max_price", nullable = false)
    @Schema(description = "최고 단가 (해당 주의 모든 거래 중 최고)", example = "150000")
    private Long maxPrice;

    @Column(name = "avg_price", nullable = false, precision = 15, scale = 2)
    @Schema(description = "평균 단가 (Daily 평균가의 평균)", example = "135000.50")
    private BigDecimal avgPrice;

    @Column(name = "total_volume", nullable = false)
    @Schema(description = "거래 총량 (총 거래 금액)", example = "35000000")
    private Long totalVolume;

    @Column(name = "total_quantity", nullable = false)
    @Schema(description = "거래 수량 (itemCount 합계)", example = "1050")
    private Long totalQuantity;

    @Column(name = "created_at", nullable = false)
    @Schema(description = "생성 일시", example = "2025-07-08T14:35:00")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Schema(description = "수정 일시", example = "2025-07-08T15:35:00")
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
