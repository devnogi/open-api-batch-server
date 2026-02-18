package until.the.eternity.auctionitem.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

/** 실시간 경매장에서 판매 중인 아이템 정보. V15 마이그레이션에서 auction_item → auction_realtime_item으로 변경됨. */
@Entity
@Table(name = "auction_realtime_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionRealtimeItem {

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
    private Instant dateAuctionExpire;

    @Column(name = "date_register", nullable = false)
    private Instant dateRegister;

    @Column(name = "item_sub_category", nullable = false)
    private String itemSubCategory;

    @Column(name = "item_top_category", nullable = false)
    private String itemTopCategory;

    @OneToMany(mappedBy = "auctionRealtimeItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionRealtimeItemOption> auctionRealtimeItemOptions;

    public AuctionRealtimeItem linkItemOptions() {
        if (this.auctionRealtimeItemOptions != null) {
            for (AuctionRealtimeItemOption o : this.auctionRealtimeItemOptions) {
                o.setAuctionRealtimeItem(this);
            }
        }
        return this;
    }
}
