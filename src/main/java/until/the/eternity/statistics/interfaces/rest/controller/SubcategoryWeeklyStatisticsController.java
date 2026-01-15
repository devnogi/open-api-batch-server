package until.the.eternity.statistics.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.statistics.application.service.SubcategoryWeeklyStatisticsService;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryWeeklyStatisticsResponse;

@RestController
@RequestMapping("/statistics/weekly/subcategories")
@RequiredArgsConstructor
@Tag(name = "서브카테고리별 주간 통계 API", description = "서브카테고리별 주간 거래 통계 조회 API")
public class SubcategoryWeeklyStatisticsController {

    private final SubcategoryWeeklyStatisticsService service;

    @GetMapping
    @Operation(
            summary = "서브카테고리별 주간 통계 조회",
            description = "탑 카테고리와 서브 카테고리로 주간 통계를 조회합니다. 최대 4개월까지 조회 가능합니다.")
    public ResponseEntity<java.util.List<SubcategoryWeeklyStatisticsResponse>>
            searchSubcategoryWeeklyStatistics(
                    @ParameterObject @ModelAttribute
                            @jakarta.validation.Valid
                            until.the.eternity.statistics.interfaces.rest.dto.request.SubcategoryWeeklyStatisticsSearchRequest
                                    request) {
        java.util.List<SubcategoryWeeklyStatisticsResponse> results =
                service.search(
                        request.topCategory(),
                        request.subCategory(),
                        request.getStartDateWithDefault(),
                        request.getEndDateWithDefault());
        return ResponseEntity.ok(results);
    }
}
