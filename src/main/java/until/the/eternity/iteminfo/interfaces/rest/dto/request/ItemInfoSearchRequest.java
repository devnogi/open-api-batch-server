package until.the.eternity.iteminfo.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "아이템 정보 검색 요청 파라미터")
public record ItemInfoSearchRequest(
        @Schema(description = "아이템 이름", example = "나뭇가지") String name,
        @Schema(description = "하위 카테고리", example = "한손검") String subCategory,
        @Schema(description = "상위 카테고리 (선택)", example = "무기") String topCategory) {}
