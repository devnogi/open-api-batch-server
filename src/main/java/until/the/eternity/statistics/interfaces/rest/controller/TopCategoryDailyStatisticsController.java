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
import until.the.eternity.statistics.application.service.TopCategoryDailyStatisticsService;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse;

@RestController
@RequestMapping("/statistics/daily/top-categories")
@RequiredArgsConstructor
@Tag(name = "탑카테고리별 일간 통계 API", description = "탑카테고리별 일간 거래 통계 조회 API")
public class TopCategoryDailyStatisticsController {

    private final TopCategoryDailyStatisticsService service;

    @GetMapping
    @Operation(
            summary = "탑카테고리별 일간 통계 목록 조회",
            description = "탑카테고리별 일간 거래 통계 목록을 페이징하여 조회합니다. 최저가, 최고가, 평균가, 거래 총량, 거래 수량 정보를 포함합니다.")
    public ResponseEntity<PageResponseDto<TopCategoryDailyStatisticsResponse>>
            getTopCategoryDailyStatistics(@ParameterObject @ModelAttribute PageRequestDto pageDto) {
        PageResponseDto<TopCategoryDailyStatisticsResponse> result =
                service.findAll(pageDto.toPageable());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "탑카테고리별 일간 통계 단건 조회", description = "ID를 통해 특정 탑카테고리의 일간 거래 통계를 조회합니다.")
    public ResponseEntity<TopCategoryDailyStatisticsResponse> getTopCategoryDailyStatisticsById(
            @Parameter(description = "통계 ID", example = "1") @PathVariable Long id) {
        TopCategoryDailyStatisticsResponse result = service.findById(id);
        return ResponseEntity.ok(result);
    }
}
