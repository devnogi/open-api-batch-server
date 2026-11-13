package until.the.eternity.iteminfo.interfaces.rest.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ItemSitemapRequest(
        @Size(max = 8192) String cursor, @Min(1) @Max(1000) Integer limit) {
    public ItemSitemapRequest {
        if (limit == null) limit = 1000;
    }
}
