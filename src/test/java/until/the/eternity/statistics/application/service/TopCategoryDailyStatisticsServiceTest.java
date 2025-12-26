package until.the.eternity.statistics.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.statistics.domain.mapper.TopCategoryDailyStatisticsMapper;
import until.the.eternity.statistics.repository.daily.TopCategoryDailyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class TopCategoryDailyStatisticsServiceTest {

    @Mock private TopCategoryDailyStatisticsRepository repository;
    @Mock private TopCategoryDailyStatisticsMapper mapper;

    @InjectMocks private TopCategoryDailyStatisticsService service;

    @Test
    @DisplayName("findById는 데이터가 없으면 예외를 발생시킨다")
    void findById_should_throw_exception_when_not_exists() {
        // given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TopCategoryDailyStatistics not found");
    }
}
