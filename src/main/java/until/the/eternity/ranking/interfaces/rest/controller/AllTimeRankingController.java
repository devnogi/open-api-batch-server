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
import until.the.eternity.ranking.application.service.AllTimeRankingService;
import until.the.eternity.ranking.interfaces.rest.dto.request.RankingSearchRequest;
import until.the.eternity.ranking.interfaces.rest.dto.response.AllTimeRankingResponse;

@RestController
@RequestMapping("/rankings/all-time")
@RequiredArgsConstructor
@Tag(name = "역대 기록 랭킹 API", description = "역대 최고 기록 랭킹 조회 API")
public class AllTimeRankingController {

    private final AllTimeRankingService allTimeRankingService;

    @GetMapping("/highest-price")
    @Operation(
            summary = "역대 최고가 거래 TOP 100",
            description = "전체 기간 중 가장 높은 단가로 거래된 아이템 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AllTimeRankingResponse>>> getAllTimeHighestPrice(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<AllTimeRankingResponse> results =
                allTimeRankingService.getAllTimeHighestPrice(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/month/largest")
    @Operation(
            summary = "이번 달 최대 거래액 TOP 100",
            description = "이번 달 거래 중 총 거래액(단가 × 수량) 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<AllTimeRankingResponse>>> getMonthLargestVolume(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<AllTimeRankingResponse> results =
                allTimeRankingService.getMonthLargestVolume(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
