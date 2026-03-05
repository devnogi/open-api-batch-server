package until.the.eternity.ranking.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 오늘의 최고가 거래 TOP N (API 1) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_PRICE_TODAY_HIGHEST,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).byLimit(#limit)")
    public List<PriceRankingResponse> getTodayHighestPrice(int limit) {
        List<Object[]> results = rankingRepository.findTodayHighestPrice(limit);
        return rankingMapper.toPriceRankingResponses(results);
    }

    /** 이번 주 최고가 아이템 TOP N (API 2) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_PRICE_WEEK_HIGHEST,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).byLimit(#limit)")
    public List<PriceRankingResponse> getWeekHighestPrice(int limit) {
        List<Object[]> results = rankingRepository.findWeekHighestPrice(limit);
        return rankingMapper.toPriceRankingResponses(results);
    }

    /** 오늘의 최대 거래액 TOP N (API 3) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_PRICE_TODAY_VOLUME,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).byLimit(#limit)")
    public List<PriceRankingResponse> getTodayLargestVolume(int limit) {
        List<Object[]> results = rankingRepository.findTodayLargestVolume(limit);
        return rankingMapper.toPriceRankingResponses(results);
    }
}
