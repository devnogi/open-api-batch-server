package until.the.eternity.iteminfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Schema(description = "아이템 정보 요약 응답 DTO")
public record ItemInfoSummaryResponse(
        @Schema(description = "아이템 이름", example = "나뭇가지") String name,
        @Schema(description = "하위 카테고리", example = "한손검") String subCategory,
        @Schema(description = "상위 카테고리", example = "무기") String topCategory) {

    public static ItemInfoSummaryResponse from(ItemInfo itemInfo) {
        return ItemInfoSummaryResponse.builder()
                .name(itemInfo.getName())
                .subCategory(itemInfo.getSubCategory())
                .topCategory(itemInfo.getTopCategory())
                .build();
    }

    public static List<ItemInfoSummaryResponse> from(List<ItemInfo> itemInfos) {
        return itemInfos.stream().map(ItemInfoSummaryResponse::from).collect(Collectors.toList());
    }
}
