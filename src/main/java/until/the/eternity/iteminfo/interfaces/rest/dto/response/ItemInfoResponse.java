package until.the.eternity.iteminfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Schema(description = "아이템 정보 응답 DTO")
public record ItemInfoResponse(
        @Schema(description = "아이템 이름", example = "나뭇가지") String name,
        @Schema(description = "상위 카테고리", example = "무기") String topCategory,
        @Schema(description = "하위 카테고리", example = "한손검") String subCategory,
        @Schema(description = "아이템 설명", example = "흔한 나뭇가지이다.") String description,
        @Schema(description = "인벤토리 가로 크기", example = "1") Byte inventoryWidth,
        @Schema(description = "인벤토리 세로 크기", example = "2") Byte inventoryHeight,
        @Schema(description = "최대 번들 가능 개수", example = "100") Integer inventoryMaxBundleCount,
        @Schema(description = "아이템 역사") String history,
        @Schema(description = "입수 방법") String acquisitionMethod,
        @Schema(description = "1개 상점 판매가") String storeSalesPrice,
        @Schema(description = "공격 속도 및 무기 타입") String weaponType,
        @Schema(description = "수리 정보") String repair,
        @Schema(description = "최대 개조 횟수") Byte maxAlterationCount) {
    public static ItemInfoResponse from(ItemInfo itemInfo) {
        return ItemInfoResponse.builder()
                .name(itemInfo.getName())
                .topCategory(itemInfo.getTopCategory())
                .subCategory(itemInfo.getSubCategory())
                .description(itemInfo.getDescription())
                .inventoryWidth(itemInfo.getInventoryWidth())
                .inventoryHeight(itemInfo.getInventoryHeight())
                .inventoryMaxBundleCount(itemInfo.getInventoryMaxBundleCount())
                .history(itemInfo.getHistory())
                .acquisitionMethod(itemInfo.getAcquisitionMethod())
                .storeSalesPrice(itemInfo.getStoreSalesPrice())
                .weaponType(itemInfo.getWeaponType())
                .repair(itemInfo.getRepair())
                .maxAlterationCount(itemInfo.getMaxAlterationCount())
                .build();
    }

    public static List<ItemInfoResponse> from(List<ItemInfo> itemInfos) {
        return itemInfos.stream().map(ItemInfoResponse::from).collect(Collectors.toList());
    }
}
