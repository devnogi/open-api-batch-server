package until.the.eternity.auctionrealtime.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.EnchantSearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.ItemOptionSearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.MetalwareSearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.PriceSearchRequest;

/** 실시간 경매장 검색 조건 DTO */
@Schema(description = "실시간 경매장 검색 조건")
public record AuctionRealtimeSearchRequest(
        @Schema(description = "아이템 이름 (like 검색)", example = "페러시우스 타이탄 블레이드") String itemName,
        @Schema(
                        description = "아이템 이름 완전 일치 검색 여부 (true: eq, false: like)",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        defaultValue = "false",
                        example = "false")
                Boolean isExactItemName,
        @Schema(description = "대분류 카테고리", example = "근거리 장비") String itemTopCategory,
        @Schema(description = "소분류 카테고리", example = "검") String itemSubCategory,
        @Schema(description = "가격 검색 조건") PriceSearchRequest priceSearchRequest,
        @Schema(description = "거래 마감 일시 조건") DateAuctionExpireRequest dateAuctionExpireRequest,
        @Schema(description = "아이템 옵션 검색 조건") ItemOptionSearchRequest itemOptionSearchRequest,
        @Schema(description = "인챈트 검색 조건") EnchantSearchRequest enchantSearchRequest,
        @Schema(
                        description = "세공 검색 조건 목록 (최대 3개, AND 조건으로 검색)",
                        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                        hidden = true)
                List<MetalwareSearchRequest> metalwareSearchRequests) {}
