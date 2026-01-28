package until.the.eternity.auctionitem.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** 실시간 경매장 아이템(auction_realtime_item)에 연결된 아이템 옵션 정보. */
@Entity
@Table(name = "auction_realtime_item_option")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionRealtimeItemOption {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_realtime_item_id", nullable = false)
    private AuctionRealtimeItem auctionRealtimeItem;

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

    public void setAuctionRealtimeItem(AuctionRealtimeItem auctionRealtimeItem) {

        // 이전 연관관계 정리
        if (this.auctionRealtimeItem != null) {
            this.auctionRealtimeItem.getAuctionRealtimeItemOptions().remove(this);
        }

        // 새 연관관계 설정
        this.auctionRealtimeItem = auctionRealtimeItem;

        // 반대 쪽 컬렉션 동기화
        if (auctionRealtimeItem != null
                && !auctionRealtimeItem.getAuctionRealtimeItemOptions().contains(this)) {
            auctionRealtimeItem.getAuctionRealtimeItemOptions().add(this);
        }
    }

    public void setOptionValue(String optionValue) {
        this.optionValue = optionValue;
    }

    public void setOptionValue2(String optionValue2) {
        this.optionValue2 = optionValue2;
    }

    public void setOptionDesc(String optionDesc) {
        this.optionDesc = optionDesc;
    }
}
