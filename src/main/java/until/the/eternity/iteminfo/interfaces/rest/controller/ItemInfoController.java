package until.the.eternity.iteminfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.iteminfo.application.service.ItemInfoService;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoPageRequestDto;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemCategoryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSyncResponse;

@RestController
@RequestMapping("/api/item-infos")
@RequiredArgsConstructor
@Tag(name = "Item Info", description = "아이템 정보 조회 API")
public class ItemInfoController {

    private final ItemInfoService itemInfoService;

    @GetMapping("/categories")
    @Operation(summary = "카테고리 정보", description = "아이템 상위 카테고리, 하위 카테고리 정보 조회")
    public ResponseEntity<ApiResponse<List<ItemCategoryResponse>>> findItemCategories() {
        List<ItemCategoryResponse> itemCategories = itemInfoService.findItemCategories();
        return ResponseEntity.ok(ApiResponse.success(itemCategories));
    }

    @Operation(summary = "모든 아이템 정보 조회", description = "시스템에 저장된 모든 아이템 정보를 조회합니다.")
    @GetMapping
    public List<ItemInfoResponse> getAllItemInfos() {
        return itemInfoService.findAll();
    }

    @Operation(summary = "상위 카테고리로 검색", description = "상위 카테고리 이름으로 아이템 정보를 검색합니다.")
    @GetMapping("/search/top-category")
    public List<ItemInfoResponse> searchItemInfosByTopCategory(
            @Parameter(description = "검색할 상위 카테고리", required = true, example = "무기") @RequestParam
                    String topCategory) {
        return itemInfoService.findByTopCategory(topCategory);
    }

    @Operation(summary = "하위 카테고리로 검색", description = "하위 카테고리 이름으로 아이템 정보를 검색합니다.")
    @GetMapping("/search/sub-category")
    public List<ItemInfoResponse> searchItemInfosBySubCategory(
            @Parameter(description = "검색할 하위 카테고리", required = true, example = "한손검") @RequestParam
                    String subCategory) {
        return itemInfoService.findBySubCategory(subCategory);
    }

    @Operation(
            summary = "아이템 상세 정보 페이지네이션 조회",
            description = "모든 아이템 정보를 페이지네이션과 함께 조회합니다. name 컬럼 기준으로 정렬됩니다.")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Page<ItemInfoResponse>>> getItemInfosDetail(
            @Valid @ModelAttribute ItemInfoPageRequestDto pageRequest) {
        Page<ItemInfoResponse> itemInfoPage =
                itemInfoService.findAllDetail(pageRequest.toPageable());
        return ResponseEntity.ok(ApiResponse.success(itemInfoPage));
    }

    @Operation(
            summary = "아이템 요약 정보 조회",
            description = "모든 아이템의 이름, 상위 카테고리, 하위 카테고리만 조회합니다. name 컬럼 기준으로 정렬됩니다.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<ItemInfoSummaryResponse>>> getItemInfosSummary(
            @Parameter(description = "정렬 방향 (ASC, DESC)", example = "ASC")
                    @RequestParam(defaultValue = "ASC")
                    SortDirection direction) {
        List<ItemInfoSummaryResponse> summaryList =
                itemInfoService.findAllSummary(direction.toSpringDirection());
        return ResponseEntity.ok(ApiResponse.success(summaryList));
    }

    @Operation(
            summary = "경매 내역에서 아이템 정보 동기화",
            description =
                    "AuctionHistory 테이블에서 아이템 정보를 조회하여 ItemInfo 테이블에 동기화합니다. "
                            + "이미 존재하는 아이템은 제외하고 새로운 아이템만 추가합니다.")
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<ItemInfoSyncResponse>> syncItemInfoFromAuctionHistory() {
        ItemInfoSyncResponse response = itemInfoService.syncItemInfoFromAuctionHistory();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
