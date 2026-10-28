package until.the.eternity.hornBugle.interfaces.external.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenApiHornBugleHistoryListResponse(
        @JsonProperty("horn_bugle_world_history")
                List<OpenApiHornBugleHistoryResponse> hornBugleWorldHistory) {}
