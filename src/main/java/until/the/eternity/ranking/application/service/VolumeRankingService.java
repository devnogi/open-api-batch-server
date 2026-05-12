package until.the.eternity.ranking.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VolumeRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 오늘의 인기 아이템 TOP N (API 4) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_VOLUME_TODAY_POPULAR,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).byLimit(#limit)",
            sync = true)
    public List<VolumeRankingResponse> getTodayPopular(int limit) {
        List<Object[]> results = rankingRepository.findTodayPopular(limit);
        return rankingMapper.toVolumeRankingResponses(results);
    }

    /** 이번 주 인기 아이템 TOP N (API 5) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_VOLUME_WEEK_POPULAR,
            key = "T(until.the.eternity.common.util.CacheKeyBuilder).byLimit(#limit)",
            sync = true)
    public List<VolumeRankingResponse> getWeekPopular(int limit) {
        List<Object[]> results = rankingRepository.findWeekPopular(limit);
        return rankingMapper.toVolumeRankingResponses(results);
    }
}
