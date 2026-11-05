package until.the.eternity.auctionhistory.domain.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum AuctionHistoryExceptionCode implements ExceptionCode {
    AUCTION_HISTORY_NOT_FOUND(NOT_FOUND, "경매장 거래 내역을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
