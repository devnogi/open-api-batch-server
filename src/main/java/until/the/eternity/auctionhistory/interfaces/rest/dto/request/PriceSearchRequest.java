package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PriceSearchRequest(
        @Schema(description = "가격 최소값", example = "0", defaultValue = "0") long PriceTo,
        @Schema(description = "가격 최대값", example = "9999999999", defaultValue = "9999999999")
                long PriceFrom) {}
