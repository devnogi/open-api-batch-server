package until.the.eternity.itemminprice.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import until.the.eternity.itemminprice.domain.dto.response.ItemDailyMinPriceResponseDto;
import until.the.eternity.itemminprice.domain.entity.ItemDailyMinPrice;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemDailyMinPriceMapper {

    ItemDailyMinPriceResponseDto toDto(ItemDailyMinPrice entity);

    List<ItemDailyMinPriceResponseDto> toDtoList(List<ItemDailyMinPrice> entities);

    @Mapping(target = "id", ignore = true)
    ItemDailyMinPrice toEntity(ItemDailyMinPriceResponseDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntity(ItemDailyMinPriceResponseDto dto, @MappingTarget ItemDailyMinPrice entity);
}
