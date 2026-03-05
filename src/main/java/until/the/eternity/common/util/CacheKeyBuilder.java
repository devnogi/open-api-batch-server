package until.the.eternity.common.util;

import org.springframework.data.domain.Pageable;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.common.request.PageRequestDto;

public final class CacheKeyBuilder {

    private CacheKeyBuilder() {}

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
