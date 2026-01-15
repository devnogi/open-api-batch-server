package until.the.eternity.statistics.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class WeekConverter {

    /**
     * 날짜 범위를 Year-Week 리스트로 변환 ISO 8601 주 번호 체계 사용 (월요일 시작)
     *
     * @param startDate 시작 일자
     * @param endDate 종료 일자
     * @return Year-Week 조합 리스트
     */
    public static List<YearWeek> convertToYearWeekList(LocalDate startDate, LocalDate endDate) {
        List<YearWeek> yearWeeks = new ArrayList<>();

        // 시작 날짜가 속한 주의 월요일
        LocalDate current = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 종료 날짜가 속한 주의 월요일
        LocalDate endWeekStart = endDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        while (!current.isAfter(endWeekStart)) {
            int year = current.get(IsoFields.WEEK_BASED_YEAR);
            int weekNumber = current.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            yearWeeks.add(new YearWeek(year, weekNumber));

            // 다음 주로 이동
            current = current.plusWeeks(1);
        }

        return yearWeeks;
    }

    /** Year-Week 조합을 나타내는 record */
    public record YearWeek(int year, int weekNumber) {}
}
