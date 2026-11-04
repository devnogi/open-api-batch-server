package until.the.eternity.totalsearch.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.totalsearch.domain.TotalSearchKeywordType;
import until.the.eternity.totalsearch.infrastructure.TotalSearchKeywordRepository;
import until.the.eternity.totalsearch.interfaces.rest.dto.TotalSearchCandidateResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TotalSearchService {

    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final TotalSearchKeywordRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.redis-prefix:oab:v2}")
    private String cachePrefix;

    @Transactional(readOnly = true)
    public List<TotalSearchCandidateResponse> search(String rawQuery, int limit) {
        String query = rawQuery.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return List.of();
        }

        Comparator<TotalSearchCandidateResponse> comparator =
                Comparator.<TotalSearchCandidateResponse>comparingInt(
                                candidate -> matchRank(candidate.keyword(), query))
                        .thenComparingInt(candidate -> typeRank(candidate.type()))
                        .thenComparing(
                                TotalSearchCandidateResponse::keyword,
                                String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(TotalSearchCandidateResponse::targetKey);

        return loadSnapshot().stream()
                .filter(candidate -> contains(candidate.keyword(), query))
                .sorted(comparator)
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean refreshCache() {
        List<TotalSearchCandidateResponse> snapshot = loadFromDatabase();
        boolean refreshed = writeSnapshot(snapshot);
        if (refreshed) {
            log.info("[Total Search] cache refreshed: size={}", snapshot.size());
        }
        return refreshed;
    }

    private List<TotalSearchCandidateResponse> loadSnapshot() {
        try {
            String cached = redisTemplate.opsForValue().get(activeCacheKey());
            if (cached != null) {
                return deserialize(cached);
            }
        } catch (RuntimeException | JsonProcessingException e) {
            log.warn(
                    "[Total Search] cache read failed; falling back to database: {}",
                    e.getMessage());
        }

        List<TotalSearchCandidateResponse> fallback = loadFromDatabase();
        writeSnapshot(fallback);
        return fallback;
    }

    private List<TotalSearchCandidateResponse> loadFromDatabase() {
        return repository.findAllByOrderByKeywordAsc().stream()
                .map(TotalSearchCandidateResponse::from)
                .toList();
    }

    private boolean writeSnapshot(List<TotalSearchCandidateResponse> snapshot) {
        String temporaryKey = activeCacheKey() + ":tmp:" + UUID.randomUUID();
        try {
            redisTemplate
                    .opsForValue()
                    .set(temporaryKey, objectMapper.writeValueAsString(snapshot), CACHE_TTL);
            redisTemplate.rename(temporaryKey, activeCacheKey());
            return true;
        } catch (RuntimeException | JsonProcessingException e) {
            log.warn(
                    "[Total Search] cache write failed; existing snapshot retained: {}",
                    e.getMessage());
            try {
                redisTemplate.delete(temporaryKey);
            } catch (RuntimeException cleanupException) {
                log.debug("[Total Search] temporary cache cleanup failed", cleanupException);
            }
            return false;
        }
    }

    private List<TotalSearchCandidateResponse> deserialize(String json)
            throws JsonProcessingException {
        JavaType type =
                objectMapper
                        .getTypeFactory()
                        .constructCollectionType(List.class, TotalSearchCandidateResponse.class);
        return objectMapper.readValue(json, type);
    }

    private String activeCacheKey() {
        return cachePrefix + ":total-search:snapshot";
    }

    private boolean contains(String keyword, String query) {
        return keyword.toLowerCase(Locale.ROOT).contains(query);
    }

    private int matchRank(String keyword, String query) {
        String normalized = keyword.toLowerCase(Locale.ROOT);
        if (normalized.equals(query)) {
            return 0;
        }
        return normalized.startsWith(query) ? 1 : 2;
    }

    private int typeRank(TotalSearchKeywordType type) {
        return type == TotalSearchKeywordType.MENU ? 0 : 1;
    }
}
