package until.the.eternity.ranking.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.ranking.application.service.VolumeRankingService;
import until.the.eternity.ranking.interfaces.rest.dto.request.RankingSearchRequest;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeRankingResponse;

import java.util.List;

@RestController
@RequestMapping("/rankings/volume")
@RequiredArgsConstructor
@Tag(name = "거래량 랭킹 API", description = "거래량 기반 인기 아이템 랭킹 조회 API")
public class VolumeRankingController {

    private final VolumeRankingService volumeRankingService;

    @GetMapping("/today/popular")
    @Operation(
            summary = "오늘의 인기 아이템 TOP 100",
            description = "오늘 거래된 아이템 중 거래 수량 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<VolumeRankingResponse>>> getTodayPopular(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<VolumeRankingResponse> results =
                volumeRankingService.getTodayPopular(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/week/popular")
    @Operation(
            summary = "이번 주 인기 아이템 TOP 100",
            description = "이번 주 거래된 아이템 중 거래 수량 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<VolumeRankingResponse>>> getWeekPopular(
            @ParameterObject @ModelAttribute @Valid RankingSearchRequest request) {
        List<VolumeRankingResponse> results =
                volumeRankingService.getWeekPopular(request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
