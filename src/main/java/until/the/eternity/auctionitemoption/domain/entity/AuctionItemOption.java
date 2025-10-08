package until.the.eternity.auctionitemoption.domain.entity;

import jakarta.persistence.*;
import java.util.UUID;
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
public class AuctionItemOption {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "auction_history_id",
            referencedColumnName = "auction_buy_id",
            nullable = false)
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

    @PrePersist
    public void createId() {
        this.id = UUID.randomUUID().toString();
    }

    public void setAuctionHistory(AuctionHistory auctionHistory) {

        // 이전 연관관계 정리
        if (this.auctionHistory != null) {
            this.auctionHistory.getAuctionItemOptions().remove(this);
        }

        // 새 연관관계 설정
        this.auctionHistory = auctionHistory;

        // 반대 쪽 컬렉션 동기화
        if (auctionHistory != null && !auctionHistory.getAuctionItemOptions().contains(this)) {
            auctionHistory.getAuctionItemOptions().add(this);
        }
    }
}
