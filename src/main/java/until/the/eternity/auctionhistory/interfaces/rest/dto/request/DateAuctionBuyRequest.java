package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래 일자 조건")
public record DateAuctionBuyRequest(
        @Schema(description = "거래 일자 시작 범위", example = "2026-02-01") String dateAuctionBuyFrom,
        @Schema(description = "거래 일자 종료 범위", example = "2026-02-11") String dateAuctionBuyTo) {}
