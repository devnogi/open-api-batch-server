package until.the.eternity.enchantinfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인챈트 정보 동기화 응답 DTO")
public record EnchantInfoSyncResponse(
        @Schema(description = "업서트된 인챈트 건수", example = "120") int upsertedCount) {}
