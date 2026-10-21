package until.the.eternity.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import org.springframework.data.domain.Sort;

/** 정렬 방향 (오름차순/내림차순) */
@Schema(description = "정렬 방향", enumAsRef = true)
public enum SortDirection {
    ASC("ASC", "오름차순"),
    DESC("DESC", "내림차순");

    private final String code;
    private final String description;

    SortDirection(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static SortDirection from(String code) {
        if (code == null) {
            return DESC; // 기본값: 내림차순
        }
        return Arrays.stream(SortDirection.values())
                .filter(direction -> direction.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(DESC);
    }

    /** 오름차순인지 확인 */
    public boolean isAscending() {
        return this == ASC;
    }

    /** 내림차순인지 확인 */
    public boolean isDescending() {
        return this == DESC;
    }

    /** Spring Data Sort.Direction으로 변환 */
    public Sort.Direction toSpringDirection() {
        return this == ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}
