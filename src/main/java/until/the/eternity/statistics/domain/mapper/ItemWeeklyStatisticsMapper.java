package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemWeeklyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface ItemWeeklyStatisticsMapper {
    ItemWeeklyStatisticsResponse toDto(ItemWeeklyStatistics entity);
}
