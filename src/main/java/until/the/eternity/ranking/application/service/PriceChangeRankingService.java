package until.the.eternity.ranking.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceChangeRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeChangeRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceChangeRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 가격 급등 TOP N (API 6) */
    public List<PriceChangeRankingResponse> getPriceSurge(int limit) {
        List<Object[]> results = rankingRepository.findPriceSurge(limit);
        return rankingMapper.toPriceChangeRankingResponses(results);
    }

    /** 가격 급락 TOP N (API 7) */
    public List<PriceChangeRankingResponse> getPriceDrop(int limit) {
        List<Object[]> results = rankingRepository.findPriceDrop(limit);
        return rankingMapper.toPriceChangeRankingResponses(results);
    }

    /** 거래량 급증 TOP N (API 8) */
    public List<VolumeChangeRankingResponse> getVolumeSurge(int limit) {
        List<Object[]> results = rankingRepository.findVolumeSurge(limit);
        return rankingMapper.toVolumeChangeRankingResponses(results);
    }
}
