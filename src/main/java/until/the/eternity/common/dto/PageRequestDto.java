package until.the.eternity.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Schema(description = "페이지 요청 파라미터")
public record PageRequestDto(
        @Schema(description = "요청할 페이지 번호 (0부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") @Min(1) @Max(100) Integer size,
        @Schema(description = "정렬 필드 (예: createdAt)", example = "createdAt") String sortBy,
        @Schema(description = "정렬 방향 (asc or desc)", example = "desc") String direction) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_DIRECTION = "desc";

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page : DEFAULT_PAGE;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        String resolvedSortBy = this.sortBy != null ? this.sortBy : DEFAULT_SORT_BY;
        Sort.Direction resolvedDirection = parseDirection(this.direction);

        return PageRequest.of(
                resolvedPage, resolvedSize, Sort.by(resolvedDirection, resolvedSortBy));
    }

    private Sort.Direction parseDirection(String dir) {
        if ("asc".equalsIgnoreCase(dir)) return Sort.Direction.ASC;
        return Sort.Direction.DESC;
    }
}
