package until.the.eternity.metalwareinfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Schema(description = "세공 정보 응답 DTO")
public record MetalwareInfoResponse(
        @Schema(description = "세공", example = "불의 연성술") String metalware) {
    public static MetalwareInfoResponse from(String metalware) {
        return MetalwareInfoResponse.builder().metalware(metalware).build();
    }

    public static List<MetalwareInfoResponse> from(List<String> metalwares) {
        return metalwares.stream().map(MetalwareInfoResponse::from).collect(Collectors.toList());
    }
}
