package until.the.eternity.common.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class PageRequestDto {

    @Schema(defaultValue = "1", minimum = "1")
    @Min(1)
    protected Integer page = 1;

    @Schema(defaultValue = "20", minimum = "10", maximum = "100")
    @Min(1) @Max(100)
    protected Integer size = 20;

    @Schema(example = "[\"createdAt,desc\"]")
    protected List<@Pattern(regexp = "^[a-zA-Z0-9_\\.]+,(asc|desc)$",
            message = "sort 형식은 'field,asc|desc' 이어야 합니다.") String> sort;

    public Pageable toPageable() {
        Sort sortObj = (sort == null || sort.isEmpty())
                ? Sort.unsorted()
                : Sort.by(
                sort.stream()
                        .map(this::parseSort)
                        .collect(Collectors.toList()));
        return PageRequest.of(page, size, sortObj);
    }

    private Sort.Order parseSort(String spec) {
        String[] tokens = spec.split(",", 2);
        return "desc".equalsIgnoreCase(tokens[1])
                ? Sort.Order.desc(tokens[0])
                : Sort.Order.asc(tokens[0]);
    }
}
