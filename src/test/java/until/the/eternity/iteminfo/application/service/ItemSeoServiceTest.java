package until.the.eternity.iteminfo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.domain.exception.ItemInfoExceptionCode;
import until.the.eternity.iteminfo.domain.repository.ItemSeoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSeoRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSitemapRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;

@ExtendWith(MockitoExtension.class)
class ItemSeoServiceTest {
    private static final ItemInfoId ID = new ItemInfoId("공유 이름", "검", "근거리 장비");
    private static final ItemSeoRequest REQUEST = new ItemSeoRequest("공유 이름", "근거리 장비", "검");
    @Mock private ItemSeoRepositoryPort repository;
    private ItemSeoService service;

    @BeforeEach
    void setUp() {
        service = new ItemSeoService(repository, new ObjectMapper());
    }

    @Test
    void missingCatalogIs404WithoutLoadingStatistics() {
        when(repository.findSeoItem(ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.find(REQUEST))
                .isInstanceOfSatisfying(
                        CustomException.class,
                        error ->
                                assertThat(error.getCode())
                                        .isEqualTo(ItemInfoExceptionCode.ITEM_INFO_NOT_FOUND));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void selectsOldRetainedDailyOnTieAndComputesWeightedPrice() {
        ItemInfo item = ItemInfo.builder().id(ID).build();
        when(repository.findSeoItem(ID))
                .thenReturn(Optional.of(new ItemSeoRepositoryPort.SeoItem(item, true)));
        LocalDate oldDate = LocalDate.of(2020, 1, 6);
        when(repository.findLastDaily(ID)).thenReturn(Optional.of(daily(oldDate)));
        when(repository.findLastWeekly(ID)).thenReturn(Optional.of(weekly(oldDate)));
        var result = service.find(REQUEST);
        assertThat(result.seoEligible()).isTrue();
        assertThat(result.lastTrade().periodType()).isEqualTo("DAILY");
        assertThat(result.lastTrade().periodStart()).isEqualTo(oldDate);
        assertThat(result.lastTrade().periodEnd()).isEqualTo(oldDate);
        assertThat(result.lastTrade().avgPrice()).isEqualByComparingTo("333.33");
        assertThat(result.lastTrade().totalVolume()).isEqualTo(1000L);
        assertThat(result.lastTrade().totalQuantity()).isEqualTo(3L);
    }

    @Test
    void picksNewerWeeklyPeriodAndPreservesEndDateAcrossYearBoundary() {
        when(repository.findSeoItem(ID))
                .thenReturn(
                        Optional.of(
                                new ItemSeoRepositoryPort.SeoItem(
                                        ItemInfo.builder().id(ID).build(), true)));
        when(repository.findLastDaily(ID)).thenReturn(Optional.of(daily(LocalDate.of(2020, 1, 1))));
        when(repository.findLastWeekly(ID))
                .thenReturn(Optional.of(weekly(LocalDate.of(2025, 12, 29))));
        var result = service.find(REQUEST);
        assertThat(result.lastTrade().periodType()).isEqualTo("WEEKLY");
        assertThat(result.lastTrade().periodEnd()).isEqualTo(LocalDate.of(2026, 1, 4));
        assertThat(result.lastTrade().avgPrice()).isEqualByComparingTo("200");
    }

    @Test
    void returnsStoredCatalogIdentityAndAnExplicitEmptyLastTrade() {
        ItemInfoId stored = new ItemInfoId("공유 이름", "검", "근거리 장비");
        when(repository.findSeoItem(ID))
                .thenReturn(
                        Optional.of(
                                new ItemSeoRepositoryPort.SeoItem(
                                        ItemInfo.builder().id(stored).description("아이템 정보").build(),
                                        true)));
        when(repository.findLastDaily(stored)).thenReturn(Optional.empty());
        when(repository.findLastWeekly(stored)).thenReturn(Optional.empty());
        var result = service.find(REQUEST);
        assertThat(result.item().name()).isEqualTo(stored.getName());
        assertThat(result.lastTrade()).isNull();
        assertThat(result.seoEligible()).isTrue();
    }

    @Test
    void emptyMetadataAndNoStatisticsRemainValidButNonIndexable() {
        when(repository.findSeoItem(ID))
                .thenReturn(
                        Optional.of(
                                new ItemSeoRepositoryPort.SeoItem(
                                        ItemInfo.builder().id(ID).build(), false)));
        when(repository.findLastDaily(ID)).thenReturn(Optional.empty());
        when(repository.findLastWeekly(ID)).thenReturn(Optional.empty());
        var result = service.find(REQUEST);
        assertThat(result.seoEligible()).isFalse();
        assertThat(result.lastTrade()).isNull();
    }

    @Test
    void statisticsFailurePropagatesRatherThanChangingIndexEligibility() {
        when(repository.findSeoItem(ID))
                .thenReturn(
                        Optional.of(
                                new ItemSeoRepositoryPort.SeoItem(
                                        ItemInfo.builder().id(ID).build(), true)));
        when(repository.findLastDaily(ID))
                .thenThrow(new IllegalStateException("database unavailable"));
        assertThatThrownBy(() -> service.find(REQUEST)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cursorRoundTripRetainsKoreanSpecialCharactersAndFullIdentity() {
        var first = new ItemInfoSummaryResponse("이름 / \"특별\"", "검/도끼", "장비 & 의상");
        var second = new ItemInfoSummaryResponse("다음", "검", "장비");
        when(repository.findSitemapItems(null, 2)).thenReturn(List.of(first, second));
        var page = service.sitemap(new ItemSitemapRequest(null, 1));
        assertThat(page.items()).containsExactly(first);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.nextCursor()).doesNotContain("=", "+", "/");
        ItemInfoId cursor = new ItemInfoId(first.name(), first.subCategory(), first.topCategory());
        when(repository.findSitemapItems(cursor, 2)).thenReturn(List.of(second));
        var next = service.sitemap(new ItemSitemapRequest(page.nextCursor(), 1));
        assertThat(next.items()).containsExactly(second);
        assertThat(next.hasNext()).isFalse();
        assertThat(next.nextCursor()).isNull();
    }

    @Test
    void defaultsToThousandRowsAndEmptyResultHasNoCursor() {
        when(repository.findSitemapItems(null, 1001)).thenReturn(List.of());
        var page = service.sitemap(new ItemSitemapRequest(null, null));
        assertThat(page.items()).isEmpty();
        assertThat(page.hasNext()).isFalse();
        assertThat(page.nextCursor()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "!bad", "=", "a", "____"})
    void invalidBase64AndUtf8CursorsAreRejectedBeforeQuery(String cursor) {
        assertThatThrownBy(() -> service.sitemap(new ItemSitemapRequest(cursor, 10)))
                .isInstanceOf(CustomException.class);
        verifyNoInteractions(repository);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "null",
                "{}",
                "[1,2,3]",
                "[\"name\",\"top\"]",
                "[\"name\",\"top\",\"\"]",
                "[\"name\",\"top\",\"sub\"] true"
            })
    void malformedJsonIdentityCursorsAreRejected(String json) {
        String cursor =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> service.sitemap(new ItemSitemapRequest(cursor, 10)))
                .isInstanceOf(CustomException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void oversizedCategoryCursorIsRejected() throws Exception {
        String json = new ObjectMapper().writeValueAsString(List.of("name", "a".repeat(26), "sub"));
        String cursor =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> service.sitemap(new ItemSitemapRequest(cursor, 10)))
                .isInstanceOf(CustomException.class);
        verifyNoInteractions(repository);
    }

    private ItemDailyStatistics daily(LocalDate date) {
        return ItemDailyStatistics.builder()
                .dateAuctionBuy(date)
                .minPrice(100L)
                .maxPrice(500L)
                .avgPrice(new BigDecimal("500"))
                .totalVolume(1000L)
                .totalQuantity(3L)
                .updatedAt(LocalDateTime.of(2020, 1, 7, 0, 10))
                .build();
    }

    private ItemWeeklyStatistics weekly(LocalDate date) {
        return ItemWeeklyStatistics.builder()
                .weekStartDate(date)
                .minPrice(100L)
                .maxPrice(500L)
                .avgPrice(new BigDecimal("500"))
                .totalVolume(1000L)
                .totalQuantity(5L)
                .build();
    }
}
