package until.the.eternity.iteminfo.interfaces.rest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ItemSeoResponse(ItemInfoResponse item, boolean seoEligible, LastTrade lastTrade) {
    public record LastTrade(
            String periodType,
            LocalDate periodStart,
            LocalDate periodEnd,
            Long minPrice,
            Long maxPrice,
            BigDecimal avgPrice,
            Long totalQuantity,
            Long totalVolume,
            LocalDateTime updatedAt) {}
}
