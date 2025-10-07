package until.the.eternity.item.interfaces.rest.dto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import until.the.eternity.common.enums.ItemCategory;

@Getter
@Builder
public class ItemCategoryResponse {

    private String subCategory;
    private String topCategory;

    public static List<ItemCategoryResponse> from() {
        return Arrays.stream(ItemCategory.values())
                .map(
                        itemCategory ->
                                ItemCategoryResponse.builder()
                                        .subCategory(itemCategory.getSubCategory())
                                        .topCategory(itemCategory.getTopCategory())
                                        .build())
                .collect(Collectors.toList());
    }
}
