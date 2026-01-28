package until.the.eternity.auctionrealtime.domain.mapper;

import org.mapstruct.*;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** OpenApiAuctionRealtimeResponse → AuctionRealtimeItem Entity 변환 Mapper. */
@Mapper(componentModel = "spring", uses = OpenApiRealtimeItemOptionMapper.class)
public interface OpenApiAuctionRealtimeMapper {

    String UNKNOWN_ITEM_NAME = "(Unknown)";

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

    @AfterMapping
    default void afterMapping(
            OpenApiAuctionRealtimeResponse dto, @MappingTarget AuctionRealtimeItem entity) {
        // item_name이 "(Unknown)"인 경우 item_display_name으로 대체
        if (UNKNOWN_ITEM_NAME.equals(entity.getItemName())) {
            entity.setItemName(dto.itemDisplayName());
        }
    }

    @IterableMapping(qualifiedByName = "toEntity(OpenApiAuctionRealtimeResponse, ItemCategory)")
    List<AuctionRealtimeItem> toEntityList(
            List<OpenApiAuctionRealtimeResponse> dtoList, @Context ItemCategory itemCategory);

    @Named("utcToKst")
    default Instant utcToKst(Instant utcTime) {
        // API에서 받은 UTC 시간에 9시간을 더하여 KST로 변환
        return utcTime != null ? utcTime.plus(9, ChronoUnit.HOURS) : null;
    }
}
