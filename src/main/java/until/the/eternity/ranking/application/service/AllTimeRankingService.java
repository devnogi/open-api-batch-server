package until.the.eternity.ranking.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.AllTimeRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllTimeRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 역대 최고가 거래 TOP N (API 11) - auction_history 전체 스캔, 1시간 TTL */
    @Cacheable(cacheNames = CacheNames.RANKING_ALLTIME_HIGHEST, key = "#limit")
    public List<AllTimeRankingResponse> getAllTimeHighestPrice(int limit) {
        List<Object[]> results = rankingRepository.findAllTimeHighestPrice(limit);
        return rankingMapper.toAllTimeRankingResponses(results);
    }

    /** 이번 달 최대 거래액 TOP N (API 12) */
    @Cacheable(cacheNames = CacheNames.RANKING_ALLTIME_MONTH_VOLUME, key = "#limit")
    public List<AllTimeRankingResponse> getMonthLargestVolume(int limit) {
        List<Object[]> results = rankingRepository.findMonthLargestVolume(limit);
        return rankingMapper.toAllTimeRankingResponses(results);
    }
}
