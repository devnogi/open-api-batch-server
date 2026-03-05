package until.the.eternity.auctionhistory.interfaces.rest.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

/** 검색 기준 (이상/이하/같음) */
@Schema(description = "검색 기준", enumAsRef = true)
public enum SearchStandard {
    UP("UP", "이상"),
    DOWN("DOWN", "이하"),
    EQUAL("EQUAL", "같음");

    private final String code;
    private final String description;

    SearchStandard(String code, String description) {
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
    public static SearchStandard from(String code) {
        return Arrays.stream(SearchStandard.values())
                .filter(standard -> standard.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    /** UP 조건인지 확인 */
    public boolean isUp() {
        return this == UP;
    }

    /** DOWN 조건인지 확인 */
    public boolean isDown() {
        return this == DOWN;
    }

    /** EQUAL 조건인지 확인 */
    public boolean isEqual() {
        return this == EQUAL;
    }
}
