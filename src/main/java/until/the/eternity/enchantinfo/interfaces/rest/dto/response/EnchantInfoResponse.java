package until.the.eternity.enchantinfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import until.the.eternity.enchantinfo.infrastructure.persistence.EnchantInfoEntity;

@Schema(description = "인챈트 정보 응답 DTO")
public record EnchantInfoResponse(
        @Schema(description = "ID") Long id,
        @Schema(description = "인챈트 이름 및 랭크", example = "파괴의(랭크 A)") String fullname,
        @Schema(description = "인챈트 이름", example = "파괴의") String name,
        @Schema(description = "랭크", example = "A") String enchantRank,
        @Schema(description = "접두 접미 구분", example = "접두") String affixPosition,
        @Schema(description = "효과") String effect,
        @Schema(description = "입수 정보") String acquiredInfo,
        @Schema(description = "성공시 풀옵션 확률") Integer fullOptionRate,
        @Schema(description = "전용 인챈트 여부") Boolean isExclusive,
        @Schema(description = "전 부위 공용 인챈트 여부") Boolean isCommonPart) {

    public static EnchantInfoResponse from(EnchantInfoEntity entity) {
        return new EnchantInfoResponse(
                entity.getId(),
                entity.getFullname(),
                entity.getName(),
                entity.getEnchantRank(),
                entity.getAffixPosition(),
                entity.getEffect(),
                entity.getAcquiredInfo(),
                entity.getFullOptionRate(),
                entity.getIsExclusive(),
                entity.getIsCommonPart());
    }
}
