package until.the.eternity.hornBugle.domain.mapper;

import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

@Mapper(componentModel = "spring")
public interface HornBugleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "serverName", expression = "java(server.getServerName())")
    @Mapping(target = "dateRegister", source = "registerTime")
    HornBugleWorldHistory toEntity(
            OpenApiHornBugleHistoryResponse dto, HornBugleServer server, Instant registerTime);

    HornBugleHistoryResponse toResponse(HornBugleWorldHistory entity);
}
