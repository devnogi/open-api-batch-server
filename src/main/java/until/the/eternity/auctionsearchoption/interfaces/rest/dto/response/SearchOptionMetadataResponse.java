package until.the.eternity.auctionsearchoption.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "검색 옵션 메타데이터 응답")
public record SearchOptionMetadataResponse(
        @Schema(description = "검색 옵션 ID", example = "1") Long id,
        @Schema(description = "검색 옵션명", example = "밸런스") String searchOptionName,
        @Schema(description = "검색 조건 상세") Map<String, FieldMetadata> searchCondition,
        @Schema(description = "정렬 순서", example = "1") Integer displayOrder) {}
