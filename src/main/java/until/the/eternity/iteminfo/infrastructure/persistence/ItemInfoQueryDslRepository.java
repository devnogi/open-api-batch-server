package until.the.eternity.iteminfo.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.QItemInfo;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;

@Repository
@RequiredArgsConstructor
public class ItemInfoQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<ItemInfo> searchWithPagination(
            ItemInfoSearchRequest searchRequest, Pageable pageable) {
        QItemInfo itemInfo = QItemInfo.itemInfo;

        BooleanBuilder whereClause = buildWhereClause(searchRequest, itemInfo);

        JPAQuery<ItemInfo> query =
                queryFactory
                        .selectFrom(itemInfo)
                        .where(whereClause)
                        .orderBy(itemInfo.id.name.asc());

        List<ItemInfo> content =
                query.offset(pageable.getOffset()).limit(pageable.getPageSize()).fetch();

        Long total =
                queryFactory.select(itemInfo.count()).from(itemInfo).where(whereClause).fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public List<ItemInfo> search(ItemInfoSearchRequest searchRequest, Pageable pageable) {
        QItemInfo itemInfo = QItemInfo.itemInfo;

        BooleanBuilder whereClause = buildWhereClause(searchRequest, itemInfo);

        JPAQuery<ItemInfo> query = queryFactory.selectFrom(itemInfo).where(whereClause);

        // Pageable의 정렬 정보 적용
        if (pageable.getSort().isSorted()) {
            pageable.getSort()
                    .forEach(
                            order -> {
                                if (order.getProperty().equals("name")
                                        || order.getProperty().equals("id.name")) {
                                    query.orderBy(
                                            order.isAscending()
                                                    ? itemInfo.id.name.asc()
                                                    : itemInfo.id.name.desc());
                                }
                            });
        } else {
            // 기본 정렬: name 오름차순
            query.orderBy(itemInfo.id.name.asc());
        }

        return query.fetch();
    }

    private BooleanBuilder buildWhereClause(
            ItemInfoSearchRequest searchRequest, QItemInfo itemInfo) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchRequest.name() != null && !searchRequest.name().isEmpty()) {
            builder.and(itemInfo.id.name.eq(searchRequest.name()));
        }

        if (searchRequest.subCategory() != null && !searchRequest.subCategory().isEmpty()) {
            builder.and(itemInfo.id.subCategory.eq(searchRequest.subCategory()));
        }

        if (searchRequest.topCategory() != null && !searchRequest.topCategory().isEmpty()) {
            builder.and(itemInfo.id.topCategory.eq(searchRequest.topCategory()));
        }

        return builder;
    }
}
