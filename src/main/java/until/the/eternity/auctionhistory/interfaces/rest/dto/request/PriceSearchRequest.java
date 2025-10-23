package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가격 검색 조건 (범위)")
public record PriceSearchRequest(
        @Schema(description = "가격 최소값", example = "0") Long priceFrom,
        @Schema(description = "가격 최대값", example = "9999999999") Long priceTo) {}
