package until.the.eternity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryCacheWarmupService;
import until.the.eternity.auctionrealtime.application.service.AuctionRealtimeCacheWarmupService;
import until.the.eternity.auctionsearchoption.application.service.AuctionSearchOptionService;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.enchantinfo.application.service.EnchantInfoService;
import until.the.eternity.hornBugle.application.service.HornBugleService;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.iteminfo.application.service.ItemInfoService;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.metalwareinfo.application.service.MetalwareAttributeInfoService;
import until.the.eternity.metalwareinfo.application.service.MetalwareInfoService;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.request.MetalwareAttributeInfoSearchRequest;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoResponse;
import until.the.eternity.ranking.application.service.*;
import until.the.eternity.ranking.util.RankingConstants;
import until.the.eternity.statistics.application.service.*;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.cache.warmup.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CacheStartupWarmupRunner implements ApplicationRunner {

    @Qualifier("applicationTaskExecutor")
    private final TaskExecutor taskExecutor;

    private final AuctionHistoryCacheWarmupService auctionHistoryCacheWarmupService;
    private final AuctionRealtimeCacheWarmupService auctionRealtimeCacheWarmupService;
    private final HornBugleService hornBugleService;
    private final AuctionSearchOptionService auctionSearchOptionService;
    private final ItemInfoService itemInfoService;
    private final EnchantInfoService enchantInfoService;
    private final MetalwareInfoService metalwareInfoService;
    private final MetalwareAttributeInfoService metalwareAttributeInfoService;
    private final PriceRankingService priceRankingService;
    private final VolumeRankingService volumeRankingService;
    private final PriceChangeRankingService priceChangeRankingService;
    private final CategoryRankingService categoryRankingService;
    private final AllTimeRankingService allTimeRankingService;
    private final ItemDailyStatisticsService itemDailyStatisticsService;
    private final SubcategoryDailyStatisticsService subcategoryDailyStatisticsService;
    private final TopCategoryDailyStatisticsService topCategoryDailyStatisticsService;
    private final ItemWeeklyStatisticsService itemWeeklyStatisticsService;
    private final SubcategoryWeeklyStatisticsService subcategoryWeeklyStatisticsService;
    private final TopCategoryWeeklyStatisticsService topCategoryWeeklyStatisticsService;

    @Value("${app.cache.warmup.ranking-limit:50}")
    private int startupRankingLimit;

    private record StatisticsWarmupTarget(
            String itemName, String topCategory, String subCategory) {}

    private record RankingCategoryWarmupTarget(String topCategory, String subCategory) {}

    @Override
    public void run(ApplicationArguments args) {
        log.info("[Cache Warmup] Startup warmup scheduled (async)");
        taskExecutor.execute(this::warmupAll);
    }

    private void warmupAll() {
        log.info("[Cache Warmup] Startup warmup started");

        tryWarm("auction-history", auctionHistoryCacheWarmupService::evictAndWarm);
        tryWarm("auction-realtime", auctionRealtimeCacheWarmupService::evictAndWarm);
        tryWarm("horn-bugle", this::warmHornBugleCaches);
        tryWarm("search-option", auctionSearchOptionService::getAllActiveSearchOptions);
        tryWarm("item-info", this::warmItemInfoCaches);
        tryWarm("enchant-info", this::warmEnchantCaches);
        tryWarm("metalware-info", this::warmMetalwareCaches);
        tryWarm("ranking", this::warmRankingCaches);
        tryWarm("statistics", this::warmStatisticsCaches);

        log.info("[Cache Warmup] Startup warmup finished");
    }

    private void warmItemInfoCaches() {
        List<ItemInfoResponse> all = itemInfoService.findAll();
        itemInfoService.findAllDetail(
                new ItemInfoSearchRequest(null, null, null), PageRequest.of(0, 20));
        itemInfoService.findAllSummary(
                new ItemInfoSearchRequest(null, null, null), Sort.Direction.ASC);

        if (!all.isEmpty()) {
            ItemInfoResponse sample = all.get(0);
            itemInfoService.findByTopCategory(sample.topCategory());
            itemInfoService.findBySubCategory(sample.subCategory());
        }
    }

    private void warmEnchantCaches() {
        enchantInfoService.findAll(PageRequest.of(0, 20));
        enchantInfoService.findAllFullnames(null);
        enchantInfoService.findAllFullnames("접두");
        enchantInfoService.findAllFullnames("접미");
    }

    private void warmMetalwareCaches() {
        List<MetalwareInfoResponse> metalwares = metalwareInfoService.findAll();
        if (!metalwares.isEmpty()) {
            String sample = metalwares.get(0).metalware();
            metalwareAttributeInfoService.search(
                    new MetalwareAttributeInfoSearchRequest(sample, 1, 25, SortDirection.ASC));
        }
    }

    private void warmHornBugleCaches() {
        hornBugleService.search(null, null, new HornBuglePageRequestDto(1, 20));
    }

    private void warmRankingCaches() {
        for (int limit : rankingWarmupLimits()) {
            priceRankingService.getTodayHighestPrice(limit);
            priceRankingService.getWeekHighestPrice(limit);
            priceRankingService.getTodayLargestVolume(limit);

            volumeRankingService.getTodayPopular(limit);
            volumeRankingService.getWeekPopular(limit);

            priceChangeRankingService.getPriceSurge(limit);
            priceChangeRankingService.getPriceDrop(limit);
            priceChangeRankingService.getVolumeSurge(limit);

            for (RankingCategoryWarmupTarget target : rankingCategoryWarmupTargets()) {
                categoryRankingService.getCategoryTopPriced(
                        target.topCategory(), target.subCategory(), limit);
                categoryRankingService.getCategoryPopular(
                        target.topCategory(), target.subCategory(), limit);
            }

            allTimeRankingService.getAllTimeHighestPrice(limit);
            allTimeRankingService.getMonthLargestVolume(limit);
        }
    }

    private Set<Integer> rankingWarmupLimits() {
        Set<Integer> limits = new LinkedHashSet<>();
        addRankingWarmupLimit(limits, 20);
        addRankingWarmupLimit(limits, RankingConstants.DEFAULT_LIMIT);
        addRankingWarmupLimit(limits, startupRankingLimit);
        return limits;
    }

    private void addRankingWarmupLimit(Set<Integer> limits, int limit) {
        if (limit <= 0) {
            return;
        }
        limits.add(Math.min(limit, RankingConstants.MAX_LIMIT));
    }

    private List<RankingCategoryWarmupTarget> rankingCategoryWarmupTargets() {
        return List.of(
                new RankingCategoryWarmupTarget(
                        ItemCategory.ONE_HANDED_WEAPON.getTopCategory(),
                        ItemCategory.ONE_HANDED_WEAPON.getSubCategory()),
                new RankingCategoryWarmupTarget("기타", "기타"),
                new RankingCategoryWarmupTarget("소모품", "포션"));
    }

    private void warmStatisticsCaches() {
        List<ItemInfoResponse> items = itemInfoService.findAll();
        if (items.isEmpty()) {
            log.info("[Cache Warmup] statistics skipped: no item info sample");
            return;
        }

        ItemInfoResponse sample = items.get(0);
        List<StatisticsWarmupTarget> targets =
                List.of(
                        new StatisticsWarmupTarget(
                                sample.name(), sample.topCategory(), sample.subCategory()),
                        new StatisticsWarmupTarget("향기로운 꿀 우유", "기타", "기타"),
                        new StatisticsWarmupTarget("축복의 포션", "소모품", "포션"));
        LocalDate dailyStart = LocalDate.now().minusDays(14);
        LocalDate weeklyStart = LocalDate.now().minusMonths(2);
        LocalDate end = LocalDate.now();

        for (StatisticsWarmupTarget target : targets) {
            itemDailyStatisticsService.search(
                    target.itemName(), target.subCategory(), target.topCategory(), dailyStart, end);
            subcategoryDailyStatisticsService.search(
                    target.topCategory(), target.subCategory(), dailyStart, end);
            topCategoryDailyStatisticsService.search(target.topCategory(), dailyStart, end);

            itemWeeklyStatisticsService.search(
                    target.itemName(),
                    target.subCategory(),
                    target.topCategory(),
                    weeklyStart,
                    end);
            subcategoryWeeklyStatisticsService.search(
                    target.topCategory(), target.subCategory(), weeklyStart, end);
            topCategoryWeeklyStatisticsService.search(target.topCategory(), weeklyStart, end);
        }
    }

    private void tryWarm(String domain, Runnable runnable) {
        try {
            runnable.run();
            log.info("[Cache Warmup] {} warmup completed", domain);
        } catch (Exception e) {
            log.warn("[Cache Warmup] {} warmup failed: {}", domain, e.getMessage(), e);
        }
    }
}
