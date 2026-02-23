package until.the.eternity.enchantinfo.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.common.enums.SortDirection;

@Schema(description = "인챈트 정보 페이지 요청 파라미터")
public record EnchantInfoPageRequestDto(
        @Schema(description = "요청할 페이지 번호 (1부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수 (10~50)", example = "20") @Min(10) @Max(50) Integer size,
        @Schema(description = "정렬 방향 (id 기준 ASC, DESC)", example = "ASC") SortDirection direction) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MIN_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.ASC;
    private static final String SORT_FIELD = "id";

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page - 1 : DEFAULT_PAGE - 1;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        resolvedSize = Math.max(MIN_SIZE, Math.min(MAX_SIZE, resolvedSize));
        SortDirection resolvedDirection =
                this.direction != null ? this.direction : DEFAULT_DIRECTION;

        return PageRequest.of(
                resolvedPage, resolvedSize, Sort.by(resolvedDirection.toSpringDirection(), SORT_FIELD));
    }
}
