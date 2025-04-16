package until.the.eternity.auction.domain.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import until.the.eternity.auction.domain.dto.AuctionHistorySearchCondition;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.model.QAuctionHistory;

@RequiredArgsConstructor
public class AuctionHistoryRepositoryImpl implements AuctionHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AuctionHistory> search(AuctionHistorySearchCondition condition, Pageable pageable) {
        QAuctionHistory ah = QAuctionHistory.auctionHistory;

        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getItemName() != null && !condition.getItemName().isBlank()) {
            builder.and(ah.itemName.containsIgnoreCase(condition.getItemName()));
        }

        if (condition.getItemSubCategory() != null && !condition.getItemSubCategory().isBlank()) {
            builder.and(ah.itemSubCategory.eq(condition.getItemSubCategory()));
        }

        List<AuctionHistory> content =
                queryFactory
                        .selectFrom(ah)
                        .where(builder)
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        long total = queryFactory.select(ah.count()).from(ah).where(builder).fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
