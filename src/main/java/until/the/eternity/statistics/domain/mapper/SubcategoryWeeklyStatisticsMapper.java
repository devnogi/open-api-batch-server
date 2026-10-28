package until.the.eternity.statistics.domain.mapper;

import org.mapstruct.Mapper;
import until.the.eternity.statistics.domain.entity.weekly.SubcategoryWeeklyStatistics;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryWeeklyStatisticsResponse;

@Mapper(componentModel = "spring")
public interface SubcategoryWeeklyStatisticsMapper {
    SubcategoryWeeklyStatisticsResponse toDto(SubcategoryWeeklyStatistics entity);
}
