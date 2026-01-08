package until.the.eternity.auctionhistory.domain.event;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 경매장 거래 내역 저장 완료 이벤트
 * AuctionHistoryScheduler가 거래 내역을 성공적으로 저장한 후 발행됩니다.
 */
@Getter
public class AuctionHistorySavedEvent {

    private final int savedCount;
    private final LocalDateTime eventTime;

    public AuctionHistorySavedEvent(int savedCount) {
        this.savedCount = savedCount;
        this.eventTime = LocalDateTime.now();
    }
}
