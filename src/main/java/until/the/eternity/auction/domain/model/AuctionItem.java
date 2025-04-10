package until.the.eternity.auction.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "auction_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItem {

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

    @Column(name = "date_auction_expire", nullable = false)
    private LocalDateTime dateAuctionExpire;

    @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemOption> itemOptions;
}
