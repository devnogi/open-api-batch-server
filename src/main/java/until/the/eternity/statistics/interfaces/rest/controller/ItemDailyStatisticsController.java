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
import until.the.eternity.statistics.application.service.ItemDailyStatisticsService;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemDailyStatisticsResponse;

@RestController
@RequestMapping("/statistics/daily/items")
@RequiredArgsConstructor
@Tag(name = "아이템별 일간 통계 API", description = "아이템별 일간 거래 통계 조회 API")
public class ItemDailyStatisticsController {

    private final ItemDailyStatisticsService service;

    @GetMapping
    @Operation(
            summary = "아이템별 일간 통계 목록 조회",
            description = "아이템별 일간 거래 통계 목록을 페이징하여 조회합니다. 최저가, 최고가, 평균가, 거래 총량, 거래 수량 정보를 포함합니다.")
    public ResponseEntity<PageResponseDto<ItemDailyStatisticsResponse>> getItemDailyStatistics(
            @ParameterObject @ModelAttribute PageRequestDto pageDto) {
        PageResponseDto<ItemDailyStatisticsResponse> result = service.findAll(pageDto.toPageable());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "아이템별 일간 통계 단건 조회", description = "ID를 통해 특정 아이템의 일간 거래 통계를 조회합니다.")
    public ResponseEntity<ItemDailyStatisticsResponse> getItemDailyStatisticsById(
            @Parameter(description = "통계 ID", example = "1") @PathVariable Long id) {
        ItemDailyStatisticsResponse result = service.findById(id);
        return ResponseEntity.ok(result);
    }
}
