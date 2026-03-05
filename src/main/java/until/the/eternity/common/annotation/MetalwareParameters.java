package until.the.eternity.common.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameters({
    @Parameter(
            name = "metalwareSearchRequests[0].metalware",
            description = "첫 번째 세공 이름 (완전 일치)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string", example = "불의 연성술")),
    @Parameter(
            name = "metalwareSearchRequests[0].levelFrom",
            description = "첫 번째 세공 레벨 시작값 (null이면 1로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", example = "1")),
    @Parameter(
            name = "metalwareSearchRequests[0].levelTo",
            description = "첫 번째 세공 레벨 종료값 (null이면 30으로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer", example = "30")),
    @Parameter(
            name = "metalwareSearchRequests[1].metalware",
            description = "두 번째 세공 이름 (완전 일치)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")),
    @Parameter(
            name = "metalwareSearchRequests[1].levelFrom",
            description = "두 번째 세공 레벨 시작값 (null이면 1로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer")),
    @Parameter(
            name = "metalwareSearchRequests[1].levelTo",
            description = "두 번째 세공 레벨 종료값 (null이면 30으로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer")),
    @Parameter(
            name = "metalwareSearchRequests[2].metalware",
            description = "세 번째 세공 이름 (완전 일치)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "string")),
    @Parameter(
            name = "metalwareSearchRequests[2].levelFrom",
            description = "세 번째 세공 레벨 시작값 (null이면 1로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer")),
    @Parameter(
            name = "metalwareSearchRequests[2].levelTo",
            description = "세 번째 세공 레벨 종료값 (null이면 30으로 처리)",
            in = ParameterIn.QUERY,
            schema = @Schema(type = "integer")),
})
public @interface MetalwareParameters {}
