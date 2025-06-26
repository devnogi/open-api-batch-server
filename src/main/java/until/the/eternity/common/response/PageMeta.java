package until.the.eternity.common.response;

import org.springframework.data.domain.Page;

public record PageMeta(
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,    // 다음 페이지 존재 여부
        boolean hasPrevious // 이전 페이지 존재 여부
) {
    public static PageMeta of(Page<?> page) {
        return new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }
}