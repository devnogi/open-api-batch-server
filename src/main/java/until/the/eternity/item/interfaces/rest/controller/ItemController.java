package until.the.eternity.item.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.item.application.service.ItemService;
import until.the.eternity.item.interfaces.rest.dto.ItemCategoryResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Tag(name = "아이템 정보 API", description = "아이템 정보 API")
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/categories")
    @Operation(summary = "카테고리 정보", description = "아이템 상위 카테고리, 하위 카테고리 정보 조회")
    public ResponseEntity<ApiResponse<List<ItemCategoryResponse>>> findItemCategories() {
        List<ItemCategoryResponse> itemCategories = itemService.findItemCategories();
        return ResponseEntity.ok(ApiResponse.success(itemCategories));
    }
}
