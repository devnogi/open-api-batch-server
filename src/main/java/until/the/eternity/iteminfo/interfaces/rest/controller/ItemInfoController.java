package until.the.eternity.iteminfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.iteminfo.application.service.ItemInfoService;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;

@RestController
@RequestMapping("/api/v1/item-infos")
@RequiredArgsConstructor
@Tag(name = "Item Info", description = "아이템 정보 조회 API")
public class ItemInfoController {

    private final ItemInfoService itemInfoService;

    @Operation(summary = "모든 아이템 정보 조회", description = "시스템에 저장된 모든 아이템 정보를 조회합니다.")
    @GetMapping
    public List<ItemInfoResponse> getAllItemInfos() {
        return itemInfoService.findAll();
    }

    @Operation(summary = "아이템 이름으로 검색", description = "아이템 이름에 포함된 키워드로 아이템 정보를 검색합니다.")
    @GetMapping("/search/name")
    public List<ItemInfoResponse> searchItemInfosByName(
            @Parameter(description = "검색할 아이템 이름 키워드", required = true, example = "나뭇가지")
                    @RequestParam
                    String name) {
        return itemInfoService.findByItemName(name);
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
}
