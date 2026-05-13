package until.the.eternity.auctionhistory.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;
import until.the.eternity.auctionitemoption.domain.entity.AuctionHistoryItemOption;

@Entity
@Table(
        name = "auction_history",
        indexes = {
            @Index(
                    name = "idx_auction_history_price_buy_id",
                    columnList = "auction_price_per_unit DESC, auction_buy_id DESC")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionHistory {

    @Id
    @Column(name = "auction_buy_id", nullable = false)
    private String auctionBuyId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_display_name", nullable = false)
    private String itemDisplayName;

    @Column(name = "item_count", nullable = false)
    private Long itemCount;

    @Column(name = "auction_price_per_unit", nullable = false)
    private Long auctionPricePerUnit;

    @Column(name = "date_auction_buy", nullable = false)
    private Instant dateAuctionBuy;

    @OneToMany(mappedBy = "auctionHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionHistoryItemOption> auctionHistoryItemOptions;

    @Column(name = "item_sub_category", nullable = false)
    private String itemSubCategory;

    @Column(name = "item_top_category", nullable = false)
    private String itemTopCategory;

    public AuctionHistory linkItemOptions() {
        if (this.auctionHistoryItemOptions != null) {
            for (AuctionHistoryItemOption o : this.auctionHistoryItemOptions) {
                o.setAuctionHistory(this);
            }
        }
        return this;
    }
}
