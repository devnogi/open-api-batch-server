package until.the.eternity.auctionhistory.domain.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;
import until.the.eternity.auctionitemoption.domain.entity.AuctionHistoryItemOption;
import until.the.eternity.common.util.SegongOptionParser;

@Mapper(componentModel = "spring")
public interface OpenApiItemOptionMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionHistory", ignore = true)
    AuctionHistoryItemOption toEntity(OpenApiAuctionItemOptionResponse itemOption);

    @AfterMapping
    default void afterMapping(
            OpenApiAuctionItemOptionResponse dto, @MappingTarget AuctionHistoryItemOption entity) {
        SegongOptionParser.ParseResult result =
                SegongOptionParser.parse(entity.getOptionType(), entity.getOptionValue());
        if (result != null) {
            entity.setOptionValue(result.optionValue());
            entity.setOptionValue2(result.optionValue2());
            entity.setOptionDesc(result.optionDesc());
        }
    }
}
