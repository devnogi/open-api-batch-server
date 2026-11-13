package until.the.eternity.iteminfo.domain.repository;

import java.util.List;
import java.util.Optional;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;

public interface ItemSeoRepositoryPort {
    record SeoItem(ItemInfo item, boolean eligible) {}

    Optional<SeoItem> findSeoItem(ItemInfoId id);

    List<ItemInfoSummaryResponse> findSitemapItems(ItemInfoId after, int limit);

    Optional<ItemDailyStatistics> findLastDaily(ItemInfoId id);

    Optional<ItemWeeklyStatistics> findLastWeekly(ItemInfoId id);
}
