package until.the.eternity.itemminprice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.itemminprice.domain.dto.response.ItemDailyMinPriceResponseDto;
import until.the.eternity.itemminprice.service.ItemDailyMinPriceService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/item-min-prices")
@Tag(name = "ItemDailyMinPrice", description = "아이템 일간 최저가 API")
public class ItemDailyMinPriceController {

    private final ItemDailyMinPriceService itemDailyMinPriceService;

    @PostMapping("/batch")
    @Operation(
            summary = "최저가 배치 실행",
            description = "auction_history로부터 오늘의 최저가 데이터를 계산해 item_daily_min_price 테이블에 upsert")
    public ResponseEntity<Void> triggerMinPriceBatch() {
        itemDailyMinPriceService.upsertTodayMinPrices();
        return ResponseEntity.ok().build();
    }

    // TODO: 페이지네이션을 해야되나 고민 중 (의상 구매를 할 때는 최저가로 볼 거 같기도 한데)
    @GetMapping
    @Operation(summary = "최저가 전체 조회", description = "item_daily_min_price 테이블의 모든 데이터를 반환합니다.")
    public ResponseEntity<List<ItemDailyMinPriceResponseDto>> getAll() {
        List<ItemDailyMinPriceResponseDto> dtos = itemDailyMinPriceService.findAll();
        return ResponseEntity.ok(dtos);
    }
}
