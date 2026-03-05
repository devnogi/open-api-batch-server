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
import until.the.eternity.ranking.application.service.CategoryRankingService;
import until.the.eternity.ranking.interfaces.rest.dto.request.CategoryRankingSearchRequest;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeRankingResponse;

import java.util.List;

@RestController
@RequestMapping("/rankings/category")
@RequiredArgsConstructor
@Tag(name = "카테고리별 랭킹 API", description = "카테고리별 아이템 랭킹 조회 API")
public class CategoryRankingController {

    private final CategoryRankingService categoryRankingService;

    @GetMapping("/top-priced")
    @Operation(
            summary = "카테고리별 최고가 TOP 100",
            description = "특정 카테고리 내 오늘 거래된 아이템 중 최고 단가 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PriceRankingResponse>>> getCategoryTopPriced(
            @ParameterObject @ModelAttribute @Valid CategoryRankingSearchRequest request) {
        List<PriceRankingResponse> results =
                categoryRankingService.getCategoryTopPriced(
                        request.topCategory(),
                        request.subCategory(),
                        request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/popular")
    @Operation(
            summary = "카테고리별 인기 아이템 TOP 100",
            description = "특정 카테고리 내 오늘 거래된 아이템 중 거래 수량 기준 TOP 100을 조회합니다.")
    public ResponseEntity<ApiResponse<List<VolumeRankingResponse>>> getCategoryPopular(
            @ParameterObject @ModelAttribute @Valid CategoryRankingSearchRequest request) {
        List<VolumeRankingResponse> results =
                categoryRankingService.getCategoryPopular(
                        request.topCategory(),
                        request.subCategory(),
                        request.getLimitWithDefault());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
