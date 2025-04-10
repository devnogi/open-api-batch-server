package until.the.eternity.auction.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ItemOptionDto {
    @JsonProperty("option_type")
    private String optionType;

    @JsonProperty("option_sub_type")
    private String optionSubType;

    @JsonProperty("option_value")
    private String optionValue;

    @JsonProperty("option_value2")
    private String optionValue2;

    @JsonProperty("option_desc")
    private String optionDesc;
}
