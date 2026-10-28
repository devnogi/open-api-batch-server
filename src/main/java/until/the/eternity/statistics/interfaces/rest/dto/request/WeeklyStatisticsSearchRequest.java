package until.the.eternity.statistics.interfaces.rest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주간 통계 검색 요청")
public record WeeklyStatisticsSearchRequest(
        @Schema(description = "아이템 이름 (부분 일치)", example = "켈틱") String itemName,
        @Schema(description = "아이템 서브 카테고리", example = "한손검") String itemSubCategory,
        @Schema(description = "아이템 탑 카테고리", example = "무기") String itemTopCategory,
        @Schema(description = "연도", example = "2025") Integer year,
        @Schema(description = "주차 번호 (시작)", example = "1") Integer weekNumberFrom,
        @Schema(description = "주차 번호 (종료)", example = "52") Integer weekNumberTo) {}
