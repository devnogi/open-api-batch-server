package until.the.eternity.auctionrealtime.domain.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AuctionRealtimeExceptionCode implements ExceptionCode {
    AUCTION_REALTIME_ITEM_NOT_FOUND(NOT_FOUND, "실시간 경매장 아이템을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
