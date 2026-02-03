package until.the.eternity.itemoptioninfo.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ItemOptionInfoRequest {

    @Schema(description = "아이템 옵션 유형", example = "STR")
    @NotBlank(message = "옵션 유형은 필수입니다.")
    private String optionType;

    @Schema(description = "아이템 옵션 하위 유형", example = "+")
    @NotBlank(message = "옵션 하위 유형은 필수입니다.")
    private String optionSubType;

    @Schema(description = "아이템 옵션 값", example = "10")
    @NotBlank(message = "옵션 값은 필수입니다.")
    private String optionValue;

    @Schema(description = "아이템 옵션 값 2", example = "10%")
    private String optionValue2;

    @Schema(description = "아이템 옵션 부가 정보", example = "힘이 10 증가합니다.")
    private String optionDesc;
}
