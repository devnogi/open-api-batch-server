package until.the.eternity.itemoption.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionitem.domain.entity.AuctionItem;

@Entity
@Table(name = "auction_item_option")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemOption {

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

    public void setAuctionHistory(AuctionHistory auctionHistory) {

        // 1️⃣ 이전 연관관계 정리
        if (this.auctionHistory != null) {
            this.auctionHistory.getItemOptions().remove(this);
        }

        // 2️⃣ 새 연관관계 설정
        this.auctionHistory = auctionHistory;

        // 3️⃣ 반대 쪽 컬렉션 동기화
        if (auctionHistory != null && !auctionHistory.getItemOptions().contains(this)) {
            auctionHistory.getItemOptions().add(this);
        }
    }
}
