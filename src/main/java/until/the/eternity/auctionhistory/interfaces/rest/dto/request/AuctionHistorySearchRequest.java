package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** 경매 히스토리 검색 조건 DTO - 페이지네이션 포함 */
public record AuctionHistorySearchRequest(
        @Schema(description = "아이템 이름 (like 검색)", example = "페러시우스 타이탄 블레이드") String itemName,
        @Schema(description = "대분류 카테고리", example = "근거리 장비") String itemTopCategory,
        @Schema(description = "소분류 카테고리", example = "검") String itemSubCategory,
        @Schema(description = "거래 가격", example = "10000000") String auction_price_per_unit,
        @Schema(description = "거래 일자", example = "검") String date_auction_buy,
        @Schema(description = "거래 금액") PriceSearchRequest priceSearchRequest) {}
