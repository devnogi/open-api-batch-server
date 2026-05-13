package until.the.eternity.auctionsearchoption.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auction_search_option_metadata",
        indexes = {
            @Index(
                    name = "idx_search_option_active_display_order",
                    columnList = "is_active, display_order")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionSearchOptionMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "search_option_name", nullable = false, length = 100)
    private String searchOptionName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "search_condition_json", nullable = false, columnDefinition = "JSON")
    private String searchConditionJson;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
