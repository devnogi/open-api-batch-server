package until.the.eternity.common.dto;

import org.springframework.data.domain.Sort;

public record PageRequestDto(
        int page,    // 1-based page index
        int size,
        String sortBy,
        Sort.Direction direction
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    public static PageRequestDto of(Integer page, Integer size, String sortBy, String direction) {
        int p = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int s = (size == null || size <= 0) ? DEFAULT_SIZE : size;
        String sort = (sortBy == null || sortBy.isBlank()) ? DEFAULT_SORT_BY : sortBy;
        Sort.Direction dir = (direction == null) ? DEFAULT_DIRECTION : Sort.Direction.fromString(direction);
        return new PageRequestDto(p, s, sort, dir);
    }
}
