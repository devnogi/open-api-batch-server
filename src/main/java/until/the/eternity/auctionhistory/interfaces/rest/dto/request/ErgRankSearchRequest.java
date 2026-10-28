package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에르그 등급 검색 조건")
public record ErgRankSearchRequest(
        @Schema(
                        description = "에르그 등급",
                        example = "S등급",
                        allowableValues = {"S등급", "A등급", "B등급"})
                String ergRank) {}
