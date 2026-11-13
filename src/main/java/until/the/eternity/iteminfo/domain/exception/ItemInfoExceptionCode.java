package until.the.eternity.iteminfo.domain.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.common.exception.ExceptionCode;

@Getter
@RequiredArgsConstructor
public enum ItemInfoExceptionCode implements ExceptionCode {
    TOP_CATEGORY_REQUIRED(BAD_REQUEST, "상위 카테고리(topCategory)는 필수 파라미터입니다."),
    ITEM_INFO_NOT_FOUND(NOT_FOUND, "아이템 정보를 찾을 수 없습니다."),
    INVALID_SITEMAP_CURSOR(BAD_REQUEST, "사이트맵 커서가 올바르지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
