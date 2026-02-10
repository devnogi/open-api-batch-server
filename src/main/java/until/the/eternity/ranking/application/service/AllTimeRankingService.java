package until.the.eternity.ranking.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.AllTimeRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AllTimeRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 역대 최고가 거래 TOP N (API 11) */
    public List<AllTimeRankingResponse> getAllTimeHighestPrice(int limit) {
        List<Object[]> results = rankingRepository.findAllTimeHighestPrice(limit);
        return rankingMapper.toAllTimeRankingResponses(results);
    }

    /** 이번 달 최대 거래액 TOP N (API 12) */
    public List<AllTimeRankingResponse> getMonthLargestVolume(int limit) {
        List<Object[]> results = rankingRepository.findMonthLargestVolume(limit);
        return rankingMapper.toAllTimeRankingResponses(results);
    }
}
