package until.the.eternity.statistics.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DateRangeValidatorTest {

    private static final LocalDate END_DATE = LocalDate.of(2026, 11, 5);

    @Test
    @DisplayName("일간 통계는 종료일을 포함한 30일 범위를 허용한다")
    void validateDailyDateRange_should_accept_30_inclusive_days() {
        assertDoesNotThrow(
                () -> DateRangeValidator.validateDailyDateRange(END_DATE.minusDays(29), END_DATE));
    }

    @Test
    @DisplayName("일간 통계는 종료일을 포함한 31일 범위를 거부한다")
    void validateDailyDateRange_should_reject_31_inclusive_days() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DateRangeValidator.validateDailyDateRange(END_DATE.minusDays(30), END_DATE));
    }

    @Test
    @DisplayName("주간 통계는 종료일을 포함한 120일 범위를 허용한다")
    void validateWeeklyDateRange_should_accept_120_inclusive_days() {
        assertDoesNotThrow(
                () ->
                        DateRangeValidator.validateWeeklyDateRange(
                                END_DATE.minusDays(119), END_DATE));
    }

    @Test
    @DisplayName("주간 통계는 종료일을 포함한 121일 범위를 거부한다")
    void validateWeeklyDateRange_should_reject_121_inclusive_days() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        DateRangeValidator.validateWeeklyDateRange(
                                END_DATE.minusDays(120), END_DATE));
    }
}
