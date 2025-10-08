package until.the.eternity.auctionitemoption.domain.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenApiAuctionItemOptionResponse(
        @JsonProperty("option_type") String optionType,
        @JsonProperty("option_sub_type") String optionSubType,
        @JsonProperty("option_value") String optionValue,
        @JsonProperty("option_value2") String optionValue2,
        @JsonProperty("option_desc") String optionDesc) {}
