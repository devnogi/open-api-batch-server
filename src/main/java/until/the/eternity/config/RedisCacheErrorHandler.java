package until.the.eternity.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Redis 장애 시 캐시 오류를 로깅만 하고 예외를 전파하지 않는 핸들러.
 *
 * <p>Redis가 다운되어도 캐시를 건너뛰고 실제 메서드(DB 조회 등)를 실행해 서비스를 유지한다.
 */
@Slf4j
public class RedisCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "[Cache] GET 실패 - cache={}, key={}, error={}",
                cache.getName(),
                key,
                exception.getMessage());
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn(
                "[Cache] PUT 실패 - cache={}, key={}, error={}",
                cache.getName(),
                key,
                exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "[Cache] EVICT 실패 - cache={}, key={}, error={}",
                cache.getName(),
                key,
                exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn(
                "[Cache] CLEAR 실패 - cache={}, error={}", cache.getName(), exception.getMessage());
    }
}
