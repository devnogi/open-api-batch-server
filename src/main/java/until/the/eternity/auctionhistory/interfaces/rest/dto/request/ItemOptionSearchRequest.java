package until.the.eternity.auctionhistory.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이템 옵션 검색 조건 통합")
public record ItemOptionSearchRequest(
        @Schema(description = "밸런스 검색 조건") BalanceSearchRequest balanceSearch,
        @Schema(description = "크리티컬 검색 조건") CriticalSearchRequest criticalSearch,
        @Schema(description = "방어력 검색 조건") DefenseSearchRequest defenseSearch,
        @Schema(description = "에르그 검색 조건") ErgSearchRequest ergSearch,
        @Schema(description = "에르그 등급 검색 조건") ErgRankSearchRequest ergRankSearch,
        @Schema(description = "마법 방어력 검색 조건") MagicDefenseSearchRequest magicDefenseSearch,
        @Schema(description = "마법 보호 검색 조건") MagicProtectSearchRequest magicProtectSearch,
        @Schema(description = "최대 공격력 검색 조건") MaxAttackSearchRequest maxAttackSearch,
        @Schema(description = "최대 내구력 검색 조건")
                MaximumDurabilitySearchRequest maximumDurabilitySearch,
        @Schema(description = "최대 부상률 검색 조건") MaxInjuryRateSearchRequest maxInjuryRateSearch,
        @Schema(description = "숙련도 검색 조건") ProficiencySearchRequest proficiencySearch,
        @Schema(description = "보호 검색 조건") ProtectSearchRequest protectSearch,
        @Schema(description = "남은 거래 횟수 검색 조건")
                RemainingTransactionCountSearchRequest remainingTransactionCountSearch,
        @Schema(description = "남은 전용 해제 가능 횟수 검색 조건")
                RemainingUnsealCountSearchRequest remainingUnsealCountSearch,
        @Schema(description = "남은 사용 횟수 검색 조건")
                RemainingUseCountSearchRequest remainingUseCountSearch,
        @Schema(description = "착용 제한 검색 조건")
                WearingRestrictionsSearchRequest wearingRestrictionsSearch) {}
