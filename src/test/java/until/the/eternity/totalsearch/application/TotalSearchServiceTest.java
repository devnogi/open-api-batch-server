package until.the.eternity.totalsearch.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import until.the.eternity.totalsearch.domain.TotalSearchKeyword;
import until.the.eternity.totalsearch.domain.TotalSearchKeywordType;
import until.the.eternity.totalsearch.infrastructure.TotalSearchKeywordRepository;
import until.the.eternity.totalsearch.interfaces.rest.dto.TotalSearchCandidateResponse;

@ExtendWith(MockitoExtension.class)
class TotalSearchServiceTest {

    @Mock private TotalSearchKeywordRepository repository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TotalSearchService service;

    @BeforeEach
    void setUp() {
        service = new TotalSearchService(repository, redisTemplate, objectMapper);
        ReflectionTestUtils.setField(service, "cachePrefix", "test");
    }

    @Test
    void searchRanksExactThenPrefixAndMenuBeforeItem() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        List<TotalSearchCandidateResponse> cached =
                List.of(
                        new TotalSearchCandidateResponse(
                                TotalSearchKeywordType.ITEM, "경매장 검", "경매장 검"),
                        new TotalSearchCandidateResponse(
                                TotalSearchKeywordType.MENU, "경매장", "AUCTION"),
                        new TotalSearchCandidateResponse(
                                TotalSearchKeywordType.ITEM, "특별 경매장 이용권", "특별 경매장 이용권"));
        when(valueOperations.get("test:total-search:snapshot"))
                .thenReturn(objectMapper.writeValueAsString(cached));

        List<TotalSearchCandidateResponse> result = service.search(" 경매장 ", 10);

        assertThat(result)
                .extracting(TotalSearchCandidateResponse::keyword)
                .containsExactly("경매장", "경매장 검", "특별 경매장 이용권");
    }

    @Test
    void searchFallsBackToDatabaseWhenRedisIsUnavailable() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        TotalSearchKeyword keyword = mock(TotalSearchKeyword.class);
        when(keyword.getType()).thenReturn(TotalSearchKeywordType.ITEM);
        when(keyword.getKeyword()).thenReturn("나이트브링어 워로드");
        when(keyword.getTargetKey()).thenReturn("나이트브링어 워로드");
        when(valueOperations.get(anyString()))
                .thenThrow(new RedisConnectionFailureException("offline"));
        when(repository.findAllByOrderByKeywordAsc()).thenReturn(List.of(keyword));

        List<TotalSearchCandidateResponse> result = service.search("워로드", 10);

        assertThat(result)
                .extracting(TotalSearchCandidateResponse::keyword)
                .containsExactly("나이트브링어 워로드");
        verify(repository).findAllByOrderByKeywordAsc();
    }

    @Test
    void refreshWritesTemporarySnapshotWithTwentyFourHourTtlThenRenamesIt() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(repository.findAllByOrderByKeywordAsc()).thenReturn(List.of());

        boolean refreshed = service.refreshCache();

        assertThat(refreshed).isTrue();
        verify(valueOperations)
                .set(
                        anyString(),
                        anyString(),
                        org.mockito.ArgumentMatchers.eq(Duration.ofHours(24)));
        verify(redisTemplate)
                .rename(anyString(), org.mockito.ArgumentMatchers.eq("test:total-search:snapshot"));
    }

    @Test
    void failedCacheWriteDoesNotReplaceActiveSnapshot() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(repository.findAllByOrderByKeywordAsc()).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new RedisConnectionFailureException("offline"))
                .when(valueOperations)
                .set(anyString(), anyString(), any(Duration.class));

        assertThat(service.refreshCache()).isFalse();

        verify(redisTemplate, never())
                .rename(anyString(), org.mockito.ArgumentMatchers.eq("test:total-search:snapshot"));
    }

    @Test
    void blankQueryReturnsEmptyWithoutLoadingCache() {
        assertThat(service.search("   ", 10)).isEmpty();
    }
}
