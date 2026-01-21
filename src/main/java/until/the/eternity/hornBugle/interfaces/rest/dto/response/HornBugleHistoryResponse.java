package until.the.eternity.hornBugle.interfaces.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "뿔피리 히스토리 응답")
public record HornBugleHistoryResponse(
        @Schema(description = "고유 식별자", example = "1") Long id,
        @Schema(description = "서버 이름", example = "류트") String serverName,
        @Schema(description = "캐릭터 이름", example = "홍길동") String characterName,
        @Schema(description = "메시지 내용", example = "안녕하세요") String message,
        @Schema(description = "발화 시각 (UTC)", example = "2026-01-21T11:25:43.000Z")
                @JsonFormat(shape = JsonFormat.Shape.STRING)
                Instant dateSend,
        @Schema(description = "수집 시각 (UTC)", example = "2026-01-21T11:30:00.000Z")
                @JsonFormat(shape = JsonFormat.Shape.STRING)
                Instant dateRegister) {}
