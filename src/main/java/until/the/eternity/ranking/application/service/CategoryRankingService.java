package until.the.eternity.ranking.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.config.CacheNames;
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
    @Cacheable(
            cacheNames = CacheNames.RANKING_CATEGORY_HIGHEST,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildRankingCategoryKey(#topCategory, #subCategory, #limit)",
            sync = true)
    public List<PriceRankingResponse> getCategoryTopPriced(
            String topCategory, String subCategory, int limit) {
        List<Object[]> results =
                hasSubCategory(subCategory)
                        ? rankingRepository.findCategoryTopPricedByTopAndSubCategory(
                                topCategory, subCategory, limit)
                        : rankingRepository.findCategoryTopPricedByTopCategory(topCategory, limit);
        return rankingMapper.toPriceRankingResponses(results);
    }

    /** 카테고리별 인기 아이템 TOP N (API 10) */
    @Cacheable(
            cacheNames = CacheNames.RANKING_CATEGORY_POPULAR,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildRankingCategoryKey(#topCategory, #subCategory, #limit)",
            sync = true)
    public List<VolumeRankingResponse> getCategoryPopular(
            String topCategory, String subCategory, int limit) {
        List<Object[]> results =
                hasSubCategory(subCategory)
                        ? rankingRepository.findCategoryPopularByTopAndSubCategory(
                                topCategory, subCategory, limit)
                        : rankingRepository.findCategoryPopularByTopCategory(topCategory, limit);
        return rankingMapper.toVolumeRankingResponses(results);
    }

    private boolean hasSubCategory(String subCategory) {
        return subCategory != null && !subCategory.isBlank();
    }
}
