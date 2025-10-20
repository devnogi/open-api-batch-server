package until.the.eternity.auctionhistory.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.entity.QAuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;

@Component
@RequiredArgsConstructor
class AuctionHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        QAuctionHistory ah = QAuctionHistory.auctionHistory;
        BooleanBuilder builder = buildPredicate(condition, ah);

        List<AuctionHistory> content =
                queryFactory
                        .selectFrom(ah)
                        .leftJoin(ah.auctionItemOptions)
                        .fetchJoin()
                        .where(builder)
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long total = queryFactory.select(ah.count()).from(ah).where(builder).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private BooleanBuilder buildPredicate(AuctionHistorySearchRequest c, QAuctionHistory ah) {
        BooleanBuilder builder = new BooleanBuilder();
        if (c.itemTopCategory() != null && !c.itemTopCategory().isBlank()) {
            builder.and(ah.itemTopCategory.eq(c.itemTopCategory()));
        }
        if (c.itemSubCategory() != null && !c.itemSubCategory().isBlank()) {
            builder.and(ah.itemSubCategory.eq(c.itemSubCategory()));
        }
        if (c.itemName() != null && !c.itemName().isBlank()) {
            builder.and(ah.itemName.containsIgnoreCase(c.itemName()));
        }
        return builder;
    }
}
