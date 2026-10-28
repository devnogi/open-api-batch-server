package until.the.eternity.metalwareinfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.metalwareinfo.infrastructure.persistence.MetalwareAttributeInfoEntity;

@Builder
@Schema(description = "세공 능력치 정보 응답 DTO")
public record MetalwareAttributeInfoResponse(
        @Schema(description = "세공", example = "행운") String metalware,
        @Schema(description = "레벨", example = "8") Byte level,
        @Schema(description = "능력치 효과", example = "12.00 증가") String attribute) {

    public static MetalwareAttributeInfoResponse from(MetalwareAttributeInfoEntity entity) {
        return MetalwareAttributeInfoResponse.builder()
                .metalware(entity.getMetalware())
                .level(entity.getLevel())
                .attribute(entity.getAttribute())
                .build();
    }
}
