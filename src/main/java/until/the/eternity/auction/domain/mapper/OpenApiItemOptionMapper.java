package until.the.eternity.auction.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auction.domain.dto.external.OpenApiItemOptionResponse;
import until.the.eternity.auction.domain.model.ItemOption;

@Mapper(componentModel = "spring")
public interface OpenApiItemOptionMapper {

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionHistory", ignore = true)
    ItemOption toEntity(OpenApiItemOptionResponse dto);
}
