package until.the.eternity.statistics.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;
import until.the.eternity.statistics.repository.weekly.ItemWeeklyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class ItemStatisticsServiceTest {

    private static final String ITEM_NAME = "켈틱 로열 나이트 소드";
    private static final String SUB_CATEGORY = "검";
    private static final String TOP_CATEGORY = "근거리 장비";
    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 5);

    @Mock private ItemDailyStatisticsRepository dailyRepository;
    @Mock private ItemWeeklyStatisticsRepository weeklyRepository;

    private ItemDailyStatisticsService dailyService;
    private ItemWeeklyStatisticsService weeklyService;

    @BeforeEach
    void setUp() {
        dailyService = new ItemDailyStatisticsService(dailyRepository);
        weeklyService = new ItemWeeklyStatisticsService(weeklyRepository);
    }

    @Test
    @DisplayName("일간 시세가 없으면 30일 범위의 빈 목록을 반환한다")
    void dailySearch_should_return_empty_result() {
        LocalDate startDate = END_DATE.minusDays(29);
        when(dailyRepository.findByItemAndDateRange(
                        ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE))
                .thenReturn(List.of());

        assertThat(dailyService.search(ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE))
                .isEmpty();
        verify(dailyRepository)
                .findByItemAndDateRange(ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE);
    }

    @Test
    @DisplayName("주간 시세가 없으면 120일 범위의 빈 목록을 반환한다")
    void weeklySearch_should_return_empty_result() {
        LocalDate startDate = END_DATE.minusDays(119);
        when(weeklyRepository.findByItemAndDateRange(
                        ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE))
                .thenReturn(List.of());

        assertThat(weeklyService.search(ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE))
                .isEmpty();
        verify(weeklyRepository)
                .findByItemAndDateRange(ITEM_NAME, SUB_CATEGORY, TOP_CATEGORY, startDate, END_DATE);
    }
}
