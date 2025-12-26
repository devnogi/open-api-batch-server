package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.weekly.TopCategoryWeeklyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryWeeklyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface TopCategoryWeeklyStatisticsMapper {
    TopCategoryWeeklyStatisticsResponse toDto(TopCategoryWeeklyStatistics entity);
}
