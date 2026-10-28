package until.the.eternity.metalwareinfo.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.common.enums.SortDirection;

@Schema(description = "세공 능력치 검색 요청 파라미터")
public record MetalwareAttributeInfoSearchRequest(
        @Schema(description = "세공 이름 (필수)", example = "행운") @NotBlank String metalware,
        @Schema(description = "요청할 페이지 번호 (1부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수", example = "25") @Min(1) @Max(100) Integer size,
        @Schema(description = "레벨 정렬 방향 (ASC, DESC)", example = "ASC") SortDirection direction) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 25;
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.ASC;

    public Pageable toPageable() {
        int resolvedPage = page != null ? page - 1 : DEFAULT_PAGE - 1;
        int resolvedSize = size != null ? size : DEFAULT_SIZE;
        SortDirection resolvedDirection = direction != null ? direction : DEFAULT_DIRECTION;

        return PageRequest.of(
                resolvedPage,
                resolvedSize,
                Sort.by(
                        Sort.Order.asc("metalware"),
                        new Sort.Order(resolvedDirection.toSpringDirection(), "level")));
    }
}
