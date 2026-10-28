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
            OpenApiAuctionItemOptionResponse dto,
            @MappingTarget AuctionHistoryItemOption.AuctionHistoryItemOptionBuilder entity) {
        SegongOptionParser.ParseResult result =
                SegongOptionParser.parse(dto.optionType(), dto.optionValue());
        if (result != null) {
            entity.optionValue(result.optionValue());
            entity.optionValue2(result.optionValue2());
            entity.optionDesc(result.optionDesc());
        }
    }
}
