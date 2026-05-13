package until.the.eternity.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("user", "일반 사용자"),
    ADMIN("admin", "관리자"),
    SUPER_ADMIN("super_admin", "최고 관리자");

    private final String code;
    private final String description;

    private static final Map<String, UserRole> CODE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toMap(UserRole::getCode, Function.identity()));

    public static Optional<UserRole> fromCode(String code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }
}
