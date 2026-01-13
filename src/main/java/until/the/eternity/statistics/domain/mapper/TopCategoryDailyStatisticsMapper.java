package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.daily.TopCategoryDailyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.TopCategoryDailyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface TopCategoryDailyStatisticsMapper {
    TopCategoryDailyStatisticsResponse toDto(TopCategoryDailyStatistics entity);
}
