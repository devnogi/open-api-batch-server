package until.the.eternity.ranking.domain.mapper;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import until.the.eternity.ranking.interfaces.rest.dto.response.AllTimeRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceChangeRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeChangeRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeRankingResponse;

@Component
public class RankingMapper {

    /**
     * Object[] -> PriceRankingResponse 변환 순서: item_name, item_top_category, item_sub_category,
     * max_price, avg_price, total_volume, total_quantity, date_auction_buy
     */
    public List<PriceRankingResponse> toPriceRankingResponses(List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(
                        i -> {
                            Object[] row = results.get(i);
                            return new PriceRankingResponse(
                                    i + 1,
                                    (String) row[0],
                                    (String) row[1],
                                    (String) row[2],
                                    toLong(row[3]),
                                    toBigDecimal(row[4]),
                                    toLong(row[5]),
                                    toLong(row[6]),
                                    toLocalDate(row[7]));
                        })
                .toList();
    }

    /**
     * Object[] -> VolumeRankingResponse 변환 순서: item_name, item_top_category, item_sub_category,
     * total_quantity, total_volume, avg_price, date_auction_buy
     */
    public List<VolumeRankingResponse> toVolumeRankingResponses(List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(
                        i -> {
                            Object[] row = results.get(i);
                            return new VolumeRankingResponse(
                                    i + 1,
                                    (String) row[0],
                                    (String) row[1],
                                    (String) row[2],
                                    toLong(row[3]),
                                    toLong(row[4]),
                                    toBigDecimal(row[5]),
                                    toLocalDate(row[6]));
                        })
                .toList();
    }

    /**
     * Object[] -> PriceChangeRankingResponse 변환 순서: item_name, item_top_category,
     * item_sub_category, today_avg_price, yesterday_avg_price, change_rate, price_change
     */
    public List<PriceChangeRankingResponse> toPriceChangeRankingResponses(List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(
                        i -> {
                            Object[] row = results.get(i);
                            return new PriceChangeRankingResponse(
                                    i + 1,
                                    (String) row[0],
                                    (String) row[1],
                                    (String) row[2],
                                    toBigDecimal(row[3]),
                                    toBigDecimal(row[4]),
                                    toBigDecimal(row[5]),
                                    toLong(row[6]));
                        })
                .toList();
    }

    /**
     * Object[] -> VolumeChangeRankingResponse 변환 순서: item_name, item_top_category,
     * item_sub_category, today_quantity, yesterday_quantity, change_rate, quantity_change
     */
    public List<VolumeChangeRankingResponse> toVolumeChangeRankingResponses(
            List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(
                        i -> {
                            Object[] row = results.get(i);
                            return new VolumeChangeRankingResponse(
                                    i + 1,
                                    (String) row[0],
                                    (String) row[1],
                                    (String) row[2],
                                    toLong(row[3]),
                                    toLong(row[4]),
                                    toBigDecimal(row[5]),
                                    toLong(row[6]));
                        })
                .toList();
    }

    /**
     * Object[] -> AllTimeRankingResponse 변환 순서: item_name, item_display_name, item_top_category,
     * item_sub_category, auction_price_per_unit, item_count, total_price, date_auction_buy
     */
    public List<AllTimeRankingResponse> toAllTimeRankingResponses(List<Object[]> results) {
        return IntStream.range(0, results.size())
                .mapToObj(
                        i -> {
                            Object[] row = results.get(i);
                            return new AllTimeRankingResponse(
                                    i + 1,
                                    (String) row[0],
                                    (String) row[1],
                                    (String) row[2],
                                    (String) row[3],
                                    toLong(row[4]),
                                    toLong(row[5]),
                                    toLong(row[6]),
                                    toInstant(row[7]));
                        })
                .toList();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return new BigDecimal(value.toString());
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof Date) {
            return ((Date) value).toLocalDate();
        }
        if (value instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) value).getTime()).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private Instant toInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant) {
            return (Instant) value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toInstant();
        }
        if (value instanceof java.util.Date) {
            return ((java.util.Date) value).toInstant();
        }
        return Instant.parse(value.toString());
    }
}
