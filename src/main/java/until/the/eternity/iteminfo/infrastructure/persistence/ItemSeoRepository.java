package until.the.eternity.iteminfo.infrastructure.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.domain.entity.QItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemSeoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.domain.entity.daily.QItemDailyStatistics;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;
import until.the.eternity.statistics.domain.entity.weekly.QItemWeeklyStatistics;

@Repository
@RequiredArgsConstructor
public class ItemSeoRepository implements ItemSeoRepositoryPort {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<SeoItem> findSeoItem(ItemInfoId id) {
        QItemInfo item = QItemInfo.itemInfo;
        BooleanExpression eligible = eligibility(item);
        Tuple row = queryFactory.select(item, eligible).from(item).where(item.id.eq(id)).fetchOne();
        return Optional.ofNullable(row)
                .map(
                        value ->
                                new SeoItem(
                                        value.get(item), Boolean.TRUE.equals(value.get(eligible))));
    }

    @Override
    public List<ItemInfoSummaryResponse> findSitemapItems(ItemInfoId after, int limit) {
        QItemInfo item = QItemInfo.itemInfo;
        BooleanExpression afterCursor = null;
        if (after != null) {
            afterCursor =
                    item.id
                            .name
                            .gt(after.getName())
                            .or(
                                    item.id
                                            .name
                                            .eq(after.getName())
                                            .and(item.id.topCategory.gt(after.getTopCategory())))
                            .or(
                                    item.id
                                            .name
                                            .eq(after.getName())
                                            .and(item.id.topCategory.eq(after.getTopCategory()))
                                            .and(item.id.subCategory.gt(after.getSubCategory())));
        }
        return queryFactory
                .select(
                        Projections.constructor(
                                ItemInfoSummaryResponse.class,
                                item.id.name,
                                item.id.subCategory,
                                item.id.topCategory))
                .from(item)
                .where(eligibility(item), afterCursor)
                .orderBy(item.id.name.asc(), item.id.topCategory.asc(), item.id.subCategory.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Optional<ItemDailyStatistics> findLastDaily(ItemInfoId id) {
        QItemDailyStatistics daily = QItemDailyStatistics.itemDailyStatistics;
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(daily)
                        .where(
                                daily.itemName.eq(id.getName()),
                                daily.itemTopCategory.eq(id.getTopCategory()),
                                daily.itemSubCategory.eq(id.getSubCategory()))
                        .orderBy(daily.dateAuctionBuy.desc(), daily.id.desc())
                        .fetchFirst());
    }

    @Override
    public Optional<ItemWeeklyStatistics> findLastWeekly(ItemInfoId id) {
        QItemWeeklyStatistics weekly = QItemWeeklyStatistics.itemWeeklyStatistics;
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(weekly)
                        .where(
                                weekly.itemName.eq(id.getName()),
                                weekly.itemTopCategory.eq(id.getTopCategory()),
                                weekly.itemSubCategory.eq(id.getSubCategory()))
                        .orderBy(weekly.weekStartDate.desc(), weekly.id.desc())
                        .fetchFirst());
    }

    /** Shared by metadata and sitemap, independent of the current chart date window. */
    private BooleanExpression eligibility(QItemInfo item) {
        QItemDailyStatistics daily = QItemDailyStatistics.itemDailyStatistics;
        QItemWeeklyStatistics weekly = QItemWeeklyStatistics.itemWeeklyStatistics;
        return nonBlank(item.description)
                .or(nonBlank(item.acquisitionMethod))
                .or(nonBlank(item.repair))
                .or(nonBlank(item.storeSalesPrice))
                .or(
                        JPAExpressions.selectOne()
                                .from(daily)
                                .where(
                                        daily.itemName.eq(item.id.name),
                                        daily.itemTopCategory.eq(item.id.topCategory),
                                        daily.itemSubCategory.eq(item.id.subCategory))
                                .exists())
                .or(
                        JPAExpressions.selectOne()
                                .from(weekly)
                                .where(
                                        weekly.itemName.eq(item.id.name),
                                        weekly.itemTopCategory.eq(item.id.topCategory),
                                        weekly.itemSubCategory.eq(item.id.subCategory))
                                .exists());
    }

    private BooleanExpression nonBlank(StringExpression value) {
        return Expressions.booleanTemplate(
                "function('regexp_like', {0}, '[^[:space:]]') = true", value);
    }
}
