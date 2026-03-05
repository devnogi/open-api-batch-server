package until.the.eternity.enchantinfo.domain.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.common.exception.ExceptionCode;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Getter
@RequiredArgsConstructor
public enum EnchantInfoExceptionCode implements ExceptionCode {
    INVALID_AFFIX_POSITION(BAD_REQUEST, "affix_position은 '접두' 또는 '접미'만 허용됩니다."),
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return this.name();
    }
}
