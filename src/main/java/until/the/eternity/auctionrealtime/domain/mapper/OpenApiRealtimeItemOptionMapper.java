package until.the.eternity.auctionrealtime.domain.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItemOption;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;
import until.the.eternity.common.util.SegongOptionParser;

/** OpenApiAuctionItemOptionResponse → AuctionRealtimeItemOption Entity 변환 Mapper. */
@Mapper(componentModel = "spring")
public interface OpenApiRealtimeItemOptionMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionRealtimeItem", ignore = true)
    AuctionRealtimeItemOption toEntity(OpenApiAuctionItemOptionResponse itemOption);

    @AfterMapping
    default void afterMapping(
            OpenApiAuctionItemOptionResponse dto, @MappingTarget AuctionRealtimeItemOption entity) {
        SegongOptionParser.ParseResult result =
                SegongOptionParser.parse(entity.getOptionType(), entity.getOptionValue());
        if (result != null) {
            entity.setOptionValue(result.optionValue());
            entity.setOptionValue2(result.optionValue2());
            entity.setOptionDesc(result.optionDesc());
        }
    }
}
