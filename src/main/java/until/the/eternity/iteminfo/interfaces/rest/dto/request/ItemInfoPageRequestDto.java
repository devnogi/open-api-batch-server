package until.the.eternity.iteminfo.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.common.enums.SortDirection;

@Schema(description = "아이템 정보 페이지 요청 파라미터")
public record ItemInfoPageRequestDto(
        @Schema(description = "요청할 페이지 번호 (1부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") @Min(1) @Max(50) Integer size,
        @Schema(description = "정렬 방향 (ASC, DESC)", example = "ASC") SortDirection direction) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.ASC;
    private static final String SORT_FIELD = "id.name";

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page - 1 : DEFAULT_PAGE - 1;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        SortDirection resolvedDirection =
                this.direction != null ? this.direction : DEFAULT_DIRECTION;

        return PageRequest.of(
                resolvedPage,
                resolvedSize,
                Sort.by(resolvedDirection.toSpringDirection(), SORT_FIELD));
    }
}
