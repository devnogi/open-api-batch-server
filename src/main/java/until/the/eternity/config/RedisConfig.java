package until.the.eternity.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    @Value("${app.cache.redis-prefix:oab:v2}")
    private String cacheKeyPrefix;

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .computePrefixWith(cacheName -> cacheKeyPrefix + ":" + cacheName + "::")
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(
                                        jsonSerializer))
                        .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configs = new HashMap<>();

        // 랭킹 - 10분 TTL (이벤트 기반 eviction으로 실시간 반영)
        Duration rankingTtl = Duration.ofMinutes(10);
        configs.put(CacheNames.RANKING_PRICE_TODAY_HIGHEST, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_PRICE_WEEK_HIGHEST, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_PRICE_TODAY_VOLUME, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_VOLUME_TODAY_POPULAR, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_VOLUME_WEEK_POPULAR, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_CHANGE_PRICE_SURGE, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_CHANGE_PRICE_DROP, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_CHANGE_VOLUME_SURGE, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_CATEGORY_HIGHEST, defaultConfig.entryTtl(rankingTtl));
        configs.put(CacheNames.RANKING_CATEGORY_POPULAR, defaultConfig.entryTtl(rankingTtl));
        // 역대 최고가 - 1시간 TTL (auction_history 전체 스캔, 잘 바뀌지 않음)
        configs.put(
                CacheNames.RANKING_ALLTIME_HIGHEST, defaultConfig.entryTtl(Duration.ofHours(1)));
        configs.put(CacheNames.RANKING_ALLTIME_MONTH_VOLUME, defaultConfig.entryTtl(rankingTtl));

        // 검색 옵션 메타데이터 - 24시간 TTL (사실상 정적 데이터)
        configs.put(
                CacheNames.SEARCH_OPTION_ALL_ACTIVE, defaultConfig.entryTtl(Duration.ofHours(24)));

        // 마스터 데이터 - 1시간 TTL (sync API 호출 시 evict)
        Duration masterTtl = Duration.ofHours(1);
        configs.put(CacheNames.ITEM_INFO_ALL, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ITEM_INFO_BY_TOP_CATEGORY, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ITEM_INFO_BY_SUB_CATEGORY, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ITEM_INFO_DETAIL, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ITEM_INFO_SUMMARY, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ENCHANT_INFO_ALL, defaultConfig.entryTtl(masterTtl));
        configs.put(CacheNames.ENCHANT_INFO_FULLNAMES, defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put(CacheNames.METALWARE_INFO_ALL, defaultConfig.entryTtl(Duration.ofHours(6)));
        configs.put(CacheNames.METALWARE_ATTRIBUTE_INFO_SEARCH, defaultConfig.entryTtl(masterTtl));

        // 뿔피리 최신 목록 - 5분 배치 데이터의 짧은 TTL 캐시
        configs.put(CacheNames.HORN_BUGLE_RECENT, defaultConfig.entryTtl(Duration.ofMinutes(2)));

        // 통계 - 30분 TTL (이벤트 기반 eviction으로 실시간 반영)
        Duration statsTtl = Duration.ofMinutes(30);
        Duration weeklyStatsTtl = Duration.ofHours(12);
        configs.put(CacheNames.STATISTICS_ITEM_DAILY, defaultConfig.entryTtl(statsTtl));
        configs.put(CacheNames.STATISTICS_SUBCATEGORY_DAILY, defaultConfig.entryTtl(statsTtl));
        configs.put(CacheNames.STATISTICS_TOPCATEGORY_DAILY, defaultConfig.entryTtl(statsTtl));
        configs.put(CacheNames.STATISTICS_ITEM_WEEKLY, defaultConfig.entryTtl(weeklyStatsTtl));
        configs.put(
                CacheNames.STATISTICS_SUBCATEGORY_WEEKLY, defaultConfig.entryTtl(weeklyStatsTtl));
        configs.put(
                CacheNames.STATISTICS_TOPCATEGORY_WEEKLY, defaultConfig.entryTtl(weeklyStatsTtl));

        // 실시간 경매 - 12분 TTL (10분 배치 + 여유 2분)
        configs.put(
                CacheNames.AUCTION_REALTIME_SEARCH, defaultConfig.entryTtl(Duration.ofMinutes(12)));

        // 경매 거래 내역 - 2시간 TTL (배치 완료 시 evict + warmup)
        configs.put(CacheNames.AUCTION_HISTORY_SEARCH, defaultConfig.entryTtl(Duration.ofHours(2)));
        configs.put(CacheNames.AUCTION_HISTORY_COUNT, defaultConfig.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}
