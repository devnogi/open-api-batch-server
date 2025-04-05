package until.the.eternity.auction.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "auction_item_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionItemOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_history_id", nullable = true)
    private AuctionHistory auctionHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = true)
    private AuctionItem auctionItem;

    @Column(name = "option_type")
    private String optionType;

    @Column(name = "option_sub_type")
    private String optionSubType;

    @Column(name = "option_value")
    private String optionValue;

    @Column(name = "option_value2")
    private String optionValue2;

    @Column(name = "option_desc", columnDefinition = "TEXT")
    private String optionDesc;
}
