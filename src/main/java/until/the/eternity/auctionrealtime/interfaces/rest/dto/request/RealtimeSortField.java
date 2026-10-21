package until.the.eternity.auctionrealtime.interfaces.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;

/** 실시간 경매장 정렬 필드 */
@Schema(description = "실시간 경매장 정렬 필드", enumAsRef = true)
public enum RealtimeSortField {
    DATE_AUCTION_EXPIRE("dateAuctionExpire", "경매 만료 일시"),
    DATE_AUCTION_REGISTER("dateAuctionRegister", "등록 일시"),
    AUCTION_PRICE_PER_UNIT("auctionPricePerUnit", "개당 가격"),
    ITEM_NAME("itemName", "아이템 이름");

    private final String fieldName;
    private final String description;

    RealtimeSortField(String fieldName, String description) {
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
    public static RealtimeSortField from(String fieldName) {
        if (fieldName == null) {
            return DATE_AUCTION_EXPIRE;
        }
        return Arrays.stream(RealtimeSortField.values())
                .filter(field -> field.fieldName.equalsIgnoreCase(fieldName))
                .findFirst()
                .orElse(DATE_AUCTION_EXPIRE);
    }
}
