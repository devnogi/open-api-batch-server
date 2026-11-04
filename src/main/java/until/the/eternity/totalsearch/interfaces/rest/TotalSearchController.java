package until.the.eternity.totalsearch.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.totalsearch.application.TotalSearchService;
import until.the.eternity.totalsearch.interfaces.rest.dto.TotalSearchCandidateResponse;

@Validated
@RestController
@RequestMapping("/api/total-search")
@RequiredArgsConstructor
@Tag(name = "Total Search", description = "아이템과 메뉴 통합 자동완성 API")
public class TotalSearchController {

    private final TotalSearchService totalSearchService;

    @GetMapping
    @Operation(summary = "통합 자동완성", description = "아이템명과 메뉴명을 부분 일치로 검색합니다.")
    public ResponseEntity<ApiResponse<List<TotalSearchCandidateResponse>>> search(
            @RequestParam @Size(max = 100) String query,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.success(totalSearchService.search(query, limit)));
    }
}
