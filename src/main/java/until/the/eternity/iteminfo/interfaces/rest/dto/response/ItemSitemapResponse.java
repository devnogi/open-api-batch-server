package until.the.eternity.iteminfo.interfaces.rest.dto.response;

import java.util.List;

public record ItemSitemapResponse(
        List<ItemInfoSummaryResponse> items, String nextCursor, boolean hasNext) {}
