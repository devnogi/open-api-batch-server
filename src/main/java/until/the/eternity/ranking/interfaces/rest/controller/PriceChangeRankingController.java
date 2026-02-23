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
import until.the.eternity.ranking.application.service.PriceChangeRankingService;
import until.the.eternity.ranking.interfaces.rest.dto.request.RankingSearchRequest;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceChangeRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeChangeRankingResponse;

@RestController
@RequestMapping("/rankings/price-change")
@RequiredArgsConstructor
@Tag(name = "가격 변동 랭킹 API", description = "어제 대비 가격/거래량 변동 랭킹 조회 API")
public class PriceChangeRankingController {

    private final PriceChangeRankingService priceChangeRankingService;

    @GetMapping("/surge")
    @Operation(
            summary = "가격 급등 TOP 100",
            description = "어제 대비 오늘 가격이 가장 많이 상승한 아이템 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceChangeRankingResponse>>> getPriceSurge(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<PriceChangeRankingResponse> results =
                priceChangeRankingService.getPriceSurge(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/drop")
    @Operation(
            summary = "가격 급락 TOP 100",
            description = "어제 대비 오늘 가격이 가장 많이 하락한 아이템 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceChangeRankingResponse>>> getPriceDrop(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<PriceChangeRankingResponse> results =
                priceChangeRankingService.getPriceDrop(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/volume-surge")
    @Operation(
            summary = "거래량 급증 TOP 100",
            description = "어제 대비 오늘 거래량이 가장 많이 증가한 아이템 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<VolumeChangeRankingResponse>>> getVolumeSurge(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<VolumeChangeRankingResponse> results =
                priceChangeRankingService.getVolumeSurge(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
