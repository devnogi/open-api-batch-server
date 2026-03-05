package until.the.eternity.common.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.request.MetalwareAttributeInfoSearchRequest;

import java.time.LocalDate;

public final class CacheKeyBuilder {

    private CacheKeyBuilder() {}

    public static String all() {
        return "all";
    }

    public static String byText(String value) {
        return normalize(value);
    }

    public static String byLimit(int limit) {
        return String.valueOf(limit);
    }

    public static String pageable(Pageable pageable) {
        return pageable.getPageNumber() + ":" + pageable.getPageSize() + ":" + pageable.getSort();
    }

    public static String buildItemInfoDetailKey(ItemInfoSearchRequest request, Pageable pageable) {
        return normalize(request.name())
                + ":"
                + normalize(request.topCategory())
                + ":"
                + normalize(request.subCategory())
                + ":"
                + pageable.getPageNumber()
                + ":"
                + pageable.getPageSize()
                + ":"
                + pageable.getSort();
    }

    public static String buildItemInfoSummaryKey(
            ItemInfoSearchRequest request, Sort.Direction direction) {
        return normalize(request.name())
                + ":"
                + normalize(request.topCategory())
                + ":"
                + normalize(request.subCategory())
                + ":"
                + direction.name();
    }

    public static String buildEnchantInfoAllKey(Pageable pageable) {
        return pageable(pageable);
    }

    public static String buildEnchantInfoFullnamesKey(String affixPosition) {
        String normalized = normalize(affixPosition);
        return normalized.isEmpty() ? "all" : normalized;
    }

    public static String buildMetalwareAttributeInfoSearchKey(
            MetalwareAttributeInfoSearchRequest request) {
        int page = request.page() != null ? request.page() : 1;
        int size = request.size() != null ? request.size() : 25;
        String direction = request.direction() != null ? request.direction().name() : "ASC";
        return normalize(request.metalware()) + ":" + page + ":" + size + ":" + direction;
    }

    public static String buildRankingCategoryKey(
            String topCategory, String subCategory, int limit) {
        return normalize(topCategory) + ":" + normalize(subCategory) + ":" + limit;
    }

    public static String buildStatisticsItemKey(
            String itemName,
            String subCategory,
            String topCategory,
            LocalDate startDate,
            LocalDate endDate) {
        return normalize(itemName)
                + ":"
                + normalize(subCategory)
                + ":"
                + normalize(topCategory)
                + ":"
                + String.valueOf(startDate)
                + ":"
                + String.valueOf(endDate);
    }

    public static String buildStatisticsSubcategoryKey(
            String subCategory, LocalDate startDate, LocalDate endDate) {
        return normalize(subCategory)
                + ":"
                + String.valueOf(startDate)
                + ":"
                + String.valueOf(endDate);
    }

    public static String buildStatisticsTopCategoryKey(
            String topCategory, LocalDate startDate, LocalDate endDate) {
        return normalize(topCategory)
                + ":"
                + String.valueOf(startDate)
                + ":"
                + String.valueOf(endDate);
    }

    public static String buildAuctionHistorySearchKey(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {
        Pageable pageable = pageRequestDto.toPageable();
        String dateFrom = "";
        String dateTo = "";
        if (requestDto.dateAuctionBuyRequest() != null) {
            dateFrom = normalize(requestDto.dateAuctionBuyRequest().dateAuctionBuyFrom());
            dateTo = normalize(requestDto.dateAuctionBuyRequest().dateAuctionBuyTo());
        }

        return pageable.getPageNumber()
                + ":"
                + pageable.getPageSize()
                + ":"
                + pageable.getSort()
                + ":"
                + normalize(requestDto.itemName())
                + ":"
                + Boolean.TRUE.equals(requestDto.isExactItemName())
                + ":"
                + normalize(requestDto.itemTopCategory())
                + ":"
                + normalize(requestDto.itemSubCategory())
                + ":"
                + dateFrom
                + ":"
                + dateTo;
    }

    public static String buildAuctionRealtimeSearchKey(
            AuctionRealtimeSearchRequest requestDto, Pageable pageable) {
        String dateFrom = "";
        String dateTo = "";
        if (requestDto.dateAuctionExpireRequest() != null) {
            dateFrom = normalize(requestDto.dateAuctionExpireRequest().dateAuctionExpireFrom());
            dateTo = normalize(requestDto.dateAuctionExpireRequest().dateAuctionExpireTo());
        }

        return normalize(requestDto.itemTopCategory())
                + ":"
                + normalize(requestDto.itemSubCategory())
                + ":"
                + normalize(requestDto.itemName())
                + ":"
                + Boolean.TRUE.equals(requestDto.isExactItemName())
                + ":"
                + dateFrom
                + ":"
                + dateTo
                + ":"
                + pageable.getPageNumber()
                + ":"
                + pageable.getPageSize()
                + ":"
                + pageable.getSort();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}
