package until.the.eternity.ranking.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.ranking.application.service.PriceRankingService;
import until.the.eternity.ranking.interfaces.rest.dto.request.RankingSearchRequest;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceRankingResponse;

@RestController
@RequestMapping("/rankings/price")
@RequiredArgsConstructor
@Tag(name = "가격 랭킹 API", description = "가격 기반 아이템 랭킹 조회 API")
public class PriceRankingController {

    private final PriceRankingService priceRankingService;

    @GetMapping("/today/highest")
    @Operation(
            summary = "오늘의 최고가 거래 TOP 100",
            description = "오늘 거래된 아이템 중 최고 단가 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceRankingResponse>>> getTodayHighestPrice(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<PriceRankingResponse> results =
                priceRankingService.getTodayHighestPrice(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/week/highest")
    @Operation(
            summary = "이번 주 최고가 아이템 TOP 100",
            description = "이번 주 거래된 아이템 중 최고 단가 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceRankingResponse>>> getWeekHighestPrice(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<PriceRankingResponse> results =
                priceRankingService.getWeekHighestPrice(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/today/largest")
    @Operation(
            summary = "오늘의 최대 거래액 TOP 100",
            description = "오늘 거래된 아이템 중 총 거래액(거래량 × 단가) 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceRankingResponse>>> getTodayLargestVolume(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<PriceRankingResponse> results =
                priceRankingService.getTodayLargestVolume(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
