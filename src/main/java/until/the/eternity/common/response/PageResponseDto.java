package until.the.eternity.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "페이지 응답 객체")
public record PageResponseDto<T>(
        @Schema(description = "데이터 리스트") List<T> items,
        @Schema(description = "페이지 메타데이터") PageMeta meta) {

    public static <T> PageResponseDto<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResponseDto<>(page.getContent(), PageMeta.of(page));
    }
}
