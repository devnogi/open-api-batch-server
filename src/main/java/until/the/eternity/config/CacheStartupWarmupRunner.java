package until.the.eternity.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryCacheWarmupService;
import until.the.eternity.auctionrealtime.application.service.AuctionRealtimeService;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.DateAuctionExpireRequest;
import until.the.eternity.auctionsearchoption.application.service.AuctionSearchOptionService;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.enums.SortDirection;
import until.the.eternity.enchantinfo.application.service.EnchantInfoService;
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
import java.util.List;

@Slf4j
@Component
@Order(200)
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "app.cache.warmup.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class CacheStartupWarmupRunner implements ApplicationRunner {

    private final AuctionHistoryCacheWarmupService auctionHistoryCacheWarmupService;
    private final AuctionRealtimeService auctionRealtimeService;
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

    @Override
    public void run(ApplicationArguments args) {
        log.info("[Cache Warmup] Startup warmup started");

        tryWarm("auction-history", auctionHistoryCacheWarmupService::evictAndWarm);
        tryWarm("auction-realtime", this::warmAuctionRealtime);
        tryWarm("search-option", auctionSearchOptionService::getAllActiveSearchOptions);
        tryWarm("item-info", this::warmItemInfoCaches);
        tryWarm("enchant-info", this::warmEnchantCaches);
        tryWarm("metalware-info", this::warmMetalwareCaches);
        tryWarm("ranking", this::warmRankingCaches);
        tryWarm("statistics", this::warmStatisticsCaches);

        log.info("[Cache Warmup] Startup warmup finished");
    }

    private void warmAuctionRealtime() {
        AuctionRealtimeSearchRequest request =
                new AuctionRealtimeSearchRequest(
                        null,
                        false,
                        null,
                        null,
                        null,
                        new DateAuctionExpireRequest(
                                LocalDate.now().minusMonths(1).toString(),
                                LocalDate.now().toString()),
                        null,
                        null,
                        null);
        auctionRealtimeService.search(
                request, PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "dateAuctionExpire")));
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

    private void warmRankingCaches() {
        int limit =
                startupRankingLimit > 0
                        ? Math.min(startupRankingLimit, RankingConstants.MAX_LIMIT)
                        : RankingConstants.DEFAULT_LIMIT;
        priceRankingService.getTodayHighestPrice(limit);
        priceRankingService.getWeekHighestPrice(limit);
        priceRankingService.getTodayLargestVolume(limit);

        volumeRankingService.getTodayPopular(limit);
        volumeRankingService.getWeekPopular(limit);

        priceChangeRankingService.getPriceSurge(limit);
        priceChangeRankingService.getPriceDrop(limit);
        priceChangeRankingService.getVolumeSurge(limit);

        String defaultTopCategory = ItemCategory.ONE_HANDED_WEAPON.getTopCategory();
        String defaultSubCategory = ItemCategory.ONE_HANDED_WEAPON.getSubCategory();
        categoryRankingService.getCategoryTopPriced(defaultTopCategory, defaultSubCategory, limit);
        categoryRankingService.getCategoryPopular(defaultTopCategory, defaultSubCategory, limit);

        allTimeRankingService.getAllTimeHighestPrice(limit);
        allTimeRankingService.getMonthLargestVolume(limit);
    }

    private void warmStatisticsCaches() {
        List<ItemInfoResponse> items = itemInfoService.findAll();
        if (items.isEmpty()) {
            log.info("[Cache Warmup] statistics skipped: no item info sample");
            return;
        }

        ItemInfoResponse sample = items.get(0);
        LocalDate dailyStart = LocalDate.now().minusDays(14);
        LocalDate weeklyStart = LocalDate.now().minusMonths(2);
        LocalDate end = LocalDate.now();

        itemDailyStatisticsService.search(
                sample.name(), sample.subCategory(), sample.topCategory(), dailyStart, end);
        subcategoryDailyStatisticsService.search(
                sample.topCategory(), sample.subCategory(), dailyStart, end);
        topCategoryDailyStatisticsService.search(sample.topCategory(), dailyStart, end);

        itemWeeklyStatisticsService.search(
                sample.name(), sample.subCategory(), sample.topCategory(), weeklyStart, end);
        subcategoryWeeklyStatisticsService.search(
                sample.topCategory(), sample.subCategory(), weeklyStart, end);
        topCategoryWeeklyStatisticsService.search(sample.topCategory(), weeklyStart, end);
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
