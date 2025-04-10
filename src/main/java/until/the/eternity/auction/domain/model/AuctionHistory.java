package until.the.eternity.auction.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "auction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "auction_buy_id", nullable = false, unique = true)
    private String auctionBuyId;

    @OneToMany(mappedBy = "auctionHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOption> itemOptions;

    @Column(name = "item_sub_category", nullable = false)
    private String itemSubCategory;

    @Column(name = "item_top_category", nullable = false)
    private String itemTopCategory;
}
