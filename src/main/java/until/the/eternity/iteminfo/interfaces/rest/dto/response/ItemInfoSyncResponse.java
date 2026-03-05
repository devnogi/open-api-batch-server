package until.the.eternity.iteminfo.interfaces.rest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(description = "아이템 정보 동기화 응답 DTO")
public record ItemInfoSyncResponse(
        @Schema(description = "동기화된 아이템 이름 목록") List<String> syncedItemNames,
        @Schema(description = "동기화된 아이템 개수") int syncedCount) {

    public static ItemInfoSyncResponse of(List<String> syncedItemNames) {
        return ItemInfoSyncResponse.builder()
                .syncedItemNames(syncedItemNames)
                .syncedCount(syncedItemNames.size())
                .build();
    }
}
