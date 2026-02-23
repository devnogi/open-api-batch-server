package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** 경매 히스토리 검색 조건 DTO - 페이지네이션 포함 */
@Schema(description = "경매 거래내역 검색 조건")
public record AuctionHistorySearchRequest(
        @Schema(description = "아이템 이름 (like 검색)", example = "페러시우스 타이탄 블레이드") String itemName,
        @Schema(
                        description = "아이템 이름 완전 일치 검색 여부 (true: eq, false: like)",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        defaultValue = "false",
                        example = "false")
                Boolean isExactItemName,
        @Schema(description = "대분류 카테고리", example = "근거리 장비") String itemTopCategory,
        @Schema(description = "소분류 카테고리", example = "검") String itemSubCategory,
        @Schema(description = "거래 일자 조건") DateAuctionBuyRequest dateAuctionBuyRequest,
        @Schema(description = "가격 검색 조건") PriceSearchRequest priceSearchRequest,
        @Schema(description = "아이템 옵션 검색 조건") ItemOptionSearchRequest itemOptionSearchRequest,
        @Schema(description = "인챈트 검색 조건") EnchantSearchRequest enchantSearchRequest) {}
