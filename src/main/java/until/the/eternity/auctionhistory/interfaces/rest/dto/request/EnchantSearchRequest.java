package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/** 인챈트 검색 조건 DTO */
@Schema(description = "인챈트 검색 조건")
public record EnchantSearchRequest(
        @Schema(description = "접두 인챈트 fullname (예: 당당한 (6랭크))") String enchantPrefix,
        @Schema(description = "접미 인챈트 fullname (예: 강한 (5랭크))") String enchantSuffix) {}
