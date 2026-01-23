package until.the.eternity.statistics.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator {

    private static final long DAILY_MAX_DAYS = 30;
    private static final long WEEKLY_MAX_DAYS = 120; // 4개월 (약 120일)

    /**
     * Daily 통계 조회 날짜 범위 검증 (최대 30일)
     *
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @throws IllegalArgumentException 날짜 범위가 유효하지 않은 경우
     */
    public static void validateDailyDateRange(LocalDate startDate, LocalDate endDate) {
        validateBasicDateRange(startDate, endDate);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > DAILY_MAX_DAYS) {
            throw new IllegalArgumentException(
                    String.format(
                            "일간 통계 조회는 최대 %d일까지만 가능합니다. 요청 기간: %d일", DAILY_MAX_DAYS, daysBetween));
        }
    }

    /**
     * Weekly 통계 조회 날짜 범위 검증 (최대 4개월, 약 120일)
     *
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @throws IllegalArgumentException 날짜 범위가 유효하지 않은 경우
     */
    public static void validateWeeklyDateRange(LocalDate startDate, LocalDate endDate) {
        validateBasicDateRange(startDate, endDate);

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > WEEKLY_MAX_DAYS) {
            throw new IllegalArgumentException(
                    String.format(
                            "주간 통계 조회는 최대 4개월(%d일)까지만 가능합니다. 요청 기간: %d일",
                            WEEKLY_MAX_DAYS, daysBetween));
        }
    }

    /**
     * 기본 날짜 범위 검증
     *
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @throws IllegalArgumentException 시작일이 종료일보다 늦은 경우
     */
    private static void validateBasicDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 일자는 종료 일자보다 이전이어야 합니다.");
        }
    }
}
