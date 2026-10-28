package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryDailyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface SubcategoryDailyStatisticsMapper {
    SubcategoryDailyStatisticsResponse toDto(SubcategoryDailyStatistics entity);
}
