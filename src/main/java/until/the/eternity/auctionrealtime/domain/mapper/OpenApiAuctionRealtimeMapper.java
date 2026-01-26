package until.the.eternity.auctionrealtime.domain.mapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/** OpenApiAuctionRealtimeResponse → AuctionRealtimeItem Entity 변환 Mapper. */
@Mapper(componentModel = "spring", uses = OpenApiRealtimeItemOptionMapper.class)
public interface OpenApiAuctionRealtimeMapper {

    @Named("toEntity(OpenApiAuctionRealtimeResponse, ItemCategory)")
    @Mapping(
            source = "dateAuctionExpire",
            target = "dateAuctionExpire",
            qualifiedByName = "utcToKst")
    @Mapping(source = "itemOptions", target = "auctionRealtimeItemOptions")
    @Mapping(target = "itemSubCategory", expression = "java(itemCategory.getSubCategory())")
    @Mapping(target = "itemTopCategory", expression = "java(itemCategory.getTopCategory())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateRegister", expression = "java(java.time.Instant.now())")
    AuctionRealtimeItem toEntity(
            OpenApiAuctionRealtimeResponse dto, @Context ItemCategory itemCategory);

    @IterableMapping(qualifiedByName = "toEntity(OpenApiAuctionRealtimeResponse, ItemCategory)")
    List<AuctionRealtimeItem> toEntityList(
            List<OpenApiAuctionRealtimeResponse> dtoList, @Context ItemCategory itemCategory);

    @Named("utcToKst")
    default Instant utcToKst(Instant utcTime) {
        // API에서 받은 UTC 시간에 9시간을 더하여 KST로 변환
        return utcTime != null ? utcTime.plus(9, ChronoUnit.HOURS) : null;
    }
}
