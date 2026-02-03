package until.the.eternity.ranking.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.ranking.domain.mapper.RankingMapper;
import until.the.eternity.ranking.interfaces.rest.dto.response.PriceRankingResponse;
import until.the.eternity.ranking.interfaces.rest.dto.response.VolumeRankingResponse;
import until.the.eternity.ranking.repository.RankingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryRankingService {

    private final RankingRepository rankingRepository;
    private final RankingMapper rankingMapper;

    /** 카테고리별 최고가 TOP N (API 9) */
    public List<PriceRankingResponse> getCategoryTopPriced(
            String topCategory, String subCategory, int limit) {
        List<Object[]> results =
                rankingRepository.findCategoryTopPriced(topCategory, subCategory, limit);
        return rankingMapper.toPriceRankingResponses(results);
    }

    /** 카테고리별 인기 아이템 TOP N (API 10) */
    public List<VolumeRankingResponse> getCategoryPopular(
            String topCategory, String subCategory, int limit) {
        List<Object[]> results =
                rankingRepository.findCategoryPopular(topCategory, subCategory, limit);
        return rankingMapper.toVolumeRankingResponses(results);
    }
}
