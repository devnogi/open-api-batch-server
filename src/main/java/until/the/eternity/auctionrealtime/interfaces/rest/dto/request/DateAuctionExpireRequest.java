package until.the.eternity.auctionrealtime.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "거래 마감 일시 조건")
public record DateAuctionExpireRequest(
        @Schema(description = "거래 마감 일시 시작 범위", example = "2026-02-01")
                String dateAuctionExpireFrom,
        @Schema(description = "거래 마감 일시 종료 범위", example = "2026-02-11")
                String dateAuctionExpireTo) {}
