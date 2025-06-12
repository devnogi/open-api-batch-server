package until.the.eternity.auction.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.*;
import com.querydsl.core.types.dsl.PathInits;
import javax.annotation.processing.Generated;

/** QAuctionHistory is a Querydsl query type for AuctionHistory */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAuctionHistory extends EntityPathBase<AuctionHistory> {

    private static final long serialVersionUID = 774239024L;

    public static final QAuctionHistory auctionHistory = new QAuctionHistory("auctionHistory");

    public final StringPath auctionBuyId = createString("auctionBuyId");

    public final NumberPath<Long> auctionPricePerUnit =
            createNumber("auctionPricePerUnit", Long.class);

    public final DateTimePath<java.time.Instant> dateAuctionBuy =
            createDateTime("dateAuctionBuy", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> itemCount = createNumber("itemCount", Long.class);

    public final StringPath itemDisplayName = createString("itemDisplayName");

    public final StringPath itemName = createString("itemName");

    public final ListPath<ItemOption, QItemOption> itemOptions =
            this.<ItemOption, QItemOption>createList(
                    "itemOptions", ItemOption.class, QItemOption.class, PathInits.DIRECT2);

    public final StringPath itemSubCategory = createString("itemSubCategory");

    public final StringPath itemTopCategory = createString("itemTopCategory");

    public QAuctionHistory(String variable) {
        super(AuctionHistory.class, forVariable(variable));
    }

    public QAuctionHistory(Path<? extends AuctionHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuctionHistory(PathMetadata metadata) {
        super(AuctionHistory.class, metadata);
    }
}
