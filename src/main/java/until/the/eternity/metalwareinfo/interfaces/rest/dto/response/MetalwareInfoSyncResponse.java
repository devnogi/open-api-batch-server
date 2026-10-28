package until.the.eternity.metalwareinfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MetalwareInfoSyncResponse(
        @Schema(description = "레벨별 능력치(level_attribute) 업서트 건수", example = "120")
                int levelAttributeUpsertedCount,
        @Schema(description = "한계 돌파 레벨(limit_break_level) 업서트 건수", example = "120")
                int limitBreakLevelUpsertedCount,
        @Schema(description = "총 업서트 건수", example = "240") int totalUpsertedCount) {}
