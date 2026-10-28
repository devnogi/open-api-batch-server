package until.the.eternity.config;

import java.net.ConnectException;
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
        logCacheError("GET", exception, cache, key);
    }

    @Override
    public void handleCachePutError(
            RuntimeException exception, Cache cache, Object key, Object value) {
        logCacheError("PUT", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logCacheError("EVICT", exception, cache, key);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logCacheError("CLEAR", exception, cache, null);
    }

    private void logCacheError(String action, RuntimeException exception, Cache cache, Object key) {
        Throwable root = getRootCause(exception);
        String cacheName = cache != null ? cache.getName() : "unknown";
        String keyText = key != null ? truncate(String.valueOf(key), 120) : "-";
        String causeType =
                root != null
                        ? root.getClass().getSimpleName()
                        : exception.getClass().getSimpleName();
        String message = root != null ? root.getMessage() : exception.getMessage();

        if (isRedisConnectionFailure(root, message)) {
            log.warn(
                    "[Cache][RedisUnavailable] action={}, cache={}, key={}, cause={}, message={}",
                    action,
                    cacheName,
                    keyText,
                    causeType,
                    sanitize(message));
            return;
        }

        log.warn(
                "[Cache][OperationFailed] action={}, cache={}, key={}, cause={}, message={}",
                action,
                cacheName,
                keyText,
                causeType,
                sanitize(message));
    }

    private boolean isRedisConnectionFailure(Throwable root, String message) {
        if (root instanceof ConnectException) {
            return true;
        }
        String normalized = message == null ? "" : message.toLowerCase();
        return normalized.contains("connection refused")
                || normalized.contains("unable to connect to redis")
                || normalized.contains("redisconnectionfailureexception");
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private String sanitize(String message) {
        return message == null ? "-" : message.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
