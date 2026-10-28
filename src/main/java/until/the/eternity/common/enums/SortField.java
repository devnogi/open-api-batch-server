package until.the.eternity.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;

/** 정렬 필드 */
@Schema(description = "정렬 필드", enumAsRef = true)
public enum SortField {
    DATE_AUCTION_BUY("dateAuctionBuy", "거래 일자"),
    AUCTION_PRICE_PER_UNIT("auctionPricePerUnit", "개당 가격"),
    ITEM_NAME("itemName", "아이템 이름");

    private final String fieldName;
    private final String description;

    SortField(String fieldName, String description) {
        this.fieldName = fieldName;
        this.description = description;
    }

    @JsonValue
    public String getFieldName() {
        return fieldName;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator
    public static SortField from(String fieldName) {
        if (fieldName == null) {
            return DATE_AUCTION_BUY; // 기본값: 거래 일자
        }
        return Arrays.stream(SortField.values())
                .filter(field -> field.fieldName.equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(DATE_AUCTION_BUY);
    }
}
