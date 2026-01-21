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
import until.the.eternity.statistics.application.service.ItemWeeklyStatisticsService;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemWeeklyStatisticsResponse;

@RestController
@RequestMapping("/statistics/weekly/items")
@RequiredArgsConstructor
@Tag(name = "아이템별 주간 통계 API", description = "아이템별 주간 거래 통계 조회 API")
public class ItemWeeklyStatisticsController {

    private final ItemWeeklyStatisticsService service;

    @GetMapping
    @Operation(
            summary = "아이템별 주간 통계 조회",
            description = "아이템 이름, 서브 카테고리, 탑 카테고리로 주간 통계를 조회합니다. 최대 4개월까지 조회 가능합니다.")
    public ResponseEntity<java.util.List<ItemWeeklyStatisticsResponse>> searchItemWeeklyStatistics(
            @ParameterObject @ModelAttribute @jakarta.validation.Valid
                    until.the.eternity.statistics.interfaces.rest.dto.request
                                    .ItemWeeklyStatisticsSearchRequest
                            request) {
        java.util.List<ItemWeeklyStatisticsResponse> results =
                service.search(
                        request.itemName(),
                        request.subCategory(),
                        request.topCategory(),
                        request.getStartDateWithDefault(),
                        request.getEndDateWithDefault());
        return ResponseEntity.ok(results);
    }
}
