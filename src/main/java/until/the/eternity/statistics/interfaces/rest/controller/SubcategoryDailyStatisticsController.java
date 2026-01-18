package until.the.eternity.statistics.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.statistics.application.service.SubcategoryDailyStatisticsService;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryDailyStatisticsResponse;

@RestController
@RequestMapping("/statistics/daily/subcategories")
@RequiredArgsConstructor
@Tag(name = "서브카테고리별 일간 통계 API", description = "서브카테고리별 일간 거래 통계 조회 API")
public class SubcategoryDailyStatisticsController {

    private final SubcategoryDailyStatisticsService service;

    @GetMapping
    @Operation(
            summary = "서브카테고리별 일간 통계 조회",
            description = "탑 카테고리와 서브 카테고리로 일간 통계를 조회합니다. 최대 30일까지 조회 가능합니다.")
    public ResponseEntity<java.util.List<SubcategoryDailyStatisticsResponse>>
            searchSubcategoryDailyStatistics(
                    @ParameterObject @ModelAttribute
                            @jakarta.validation.Valid
                            until.the.eternity.statistics.interfaces.rest.dto.request.SubcategoryDailyStatisticsSearchRequest
                                    request) {
        java.util.List<SubcategoryDailyStatisticsResponse> results =
                service.search(
                        request.topCategory(),
                        request.subCategory(),
                        request.getStartDateWithDefault(),
                        request.getEndDateWithDefault());
        return ResponseEntity.ok(results);
    }
}
