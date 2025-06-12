package until.the.eternity.common.exception.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE("400", "잘못된 입력입니다."),
    METHOD_NOT_ALLOWED("405", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR("500", "서버 오류가 발생했습니다."),
    ENTITY_NOT_FOUND("404", "존재하지 않는 리소스입니다."),
    UNAUTHORIZED("401", "인증이 필요합니다.");

    private final String code;
    private final String message;
}
