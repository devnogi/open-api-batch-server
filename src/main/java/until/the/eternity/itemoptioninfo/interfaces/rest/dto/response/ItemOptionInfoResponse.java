package until.the.eternity.itemoptioninfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemOptionInfoResponse {

    @Schema(description = "아이템 옵션 유형", example = "STR")
    private String optionType;

    @Schema(description = "아이템 옵션 하위 유형", example = "+")
    private String optionSubType;

    @Schema(description = "아이템 옵션 값", example = "10")
    private String optionValue;

    @Schema(description = "아이템 옵션 값 2", example = "10%")
    private String optionValue2;

    @Schema(description = "아이템 옵션 부가 정보", example = "힘이 10 증가합니다.")
    private String optionDesc;
}
