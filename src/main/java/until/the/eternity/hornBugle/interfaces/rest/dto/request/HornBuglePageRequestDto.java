package until.the.eternity.hornBugle.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Schema(description = "뿔피리 히스토리 페이지 요청 파라미터")
public record HornBuglePageRequestDto(
        @Schema(description = "요청할 페이지 번호 (1부터 시작)", example = "1") @Min(1) Integer page,
        @Schema(description = "페이지당 항목 수 (최소 1, 최대 50)", example = "20") @Min(1) @Max(50)
                Integer size) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final String SORT_BY_DATE_SEND = "dateSend";

    public Pageable toPageable() {
        int resolvedPage = this.page != null ? this.page - 1 : DEFAULT_PAGE - 1;
        int resolvedSize = this.size != null ? this.size : DEFAULT_SIZE;

        return PageRequest.of(
                resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, SORT_BY_DATE_SEND));
    }

    public int getResolvedPage() {
        return this.page != null ? this.page : DEFAULT_PAGE;
    }

    public int getResolvedSize() {
        return this.size != null ? this.size : DEFAULT_SIZE;
    }
}
