package until.the.eternity.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.common.enums.SortField;

@Schema(description = "페이지 요청 파라미터")
public record PageRequestDto(
        @Schema(description = "요청할 페이지 번호 (1부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수", example = "20") @Min(1) @Max(100) Integer size,
        @Schema(
                        description = "정렬 필드 (dateAuctionBuy, auctionPricePerUnit, itemName)",
                        example = "dateAuctionBuy")
                SortField sortBy,
        @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC") SortDirection direction) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final SortField DEFAULT_SORT_BY = SortField.DATE_AUCTION_BUY;
    private static final SortDirection DEFAULT_DIRECTION = SortDirection.DESC;

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page - 1 : DEFAULT_PAGE;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;
        SortField resolvedSortBy = this.sortBy != null ? this.sortBy : DEFAULT_SORT_BY;
        SortDirection resolvedDirection =
                this.direction != null ? this.direction : DEFAULT_DIRECTION;

        return PageRequest.of(
                resolvedPage,
                resolvedSize,
                Sort.by(resolvedDirection.toSpringDirection(), resolvedSortBy.getFieldName()));
    }
}
