package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/** 경매 히스토리 검색 조건 DTO - 페이지네이션 포함 */
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionHistorySearchRequest {

    @Schema(description = "아이템 이름 (like 검색)", example = "페러시우스 타이탄 블레이드")
    private String itemName;

    @Schema(description = "대분류 카테고리", example = "근거리 장비")
    private String itemTopCategory;

    @Schema(description = "소분류 카테고리", example = "검")
    private String itemSubCategory;
}
