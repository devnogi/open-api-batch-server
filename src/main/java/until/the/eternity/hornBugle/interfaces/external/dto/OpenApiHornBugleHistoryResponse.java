package until.the.eternity.hornBugle.interfaces.external.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record OpenApiHornBugleHistoryResponse(
        @JsonProperty("character_name") String characterName,
        @JsonProperty("message") String message,
        @JsonProperty("date_send")
                @JsonFormat(
                        shape = JsonFormat.Shape.STRING,
                        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                Instant dateSend) {}
