package until.the.eternity.auctionrealtime.domain.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItemOption;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;

/** AuctionRealtimeItem Entity to DTO mapper class. */
@Mapper(componentModel = "spring")
public interface AuctionRealtimeMapper {

    @Mapping(target = "itemOptions", source = "auctionRealtimeItemOptions")
    AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> toDto(AuctionRealtimeItem entity);

    RealtimeItemOptionResponse toDto(AuctionRealtimeItemOption entity);

    List<RealtimeItemOptionResponse> toDtoList(List<AuctionRealtimeItemOption> entityList);
}
