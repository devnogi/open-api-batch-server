package until.the.eternity.auctionsearchoption.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "검색 조건 필드 메타데이터")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FieldMetadata(
        @Schema(description = "필드 타입", example = "tinyint") String type,
        @Schema(description = "필수 여부", example = "false") Boolean required,
        @Schema(description = "허용된 값 목록 (Enum인 경우)", example = "[\"UP\", \"DOWN\"]")
                List<String> allowedValues) {}
