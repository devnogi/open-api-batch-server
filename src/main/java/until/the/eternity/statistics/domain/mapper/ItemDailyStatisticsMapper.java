package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemDailyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface ItemDailyStatisticsMapper {
    ItemDailyStatisticsResponse toDto(ItemDailyStatistics entity);
}
