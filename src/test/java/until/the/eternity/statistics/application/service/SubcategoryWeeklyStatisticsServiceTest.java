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
import until.the.eternity.statistics.domain.mapper.SubcategoryWeeklyStatisticsMapper;
import until.the.eternity.statistics.repository.weekly.SubcategoryWeeklyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class SubcategoryWeeklyStatisticsServiceTest {

    @Mock private SubcategoryWeeklyStatisticsRepository repository;
    @Mock private SubcategoryWeeklyStatisticsMapper mapper;

    @InjectMocks private SubcategoryWeeklyStatisticsService service;

    @Test
    @DisplayName("findById는 데이터가 없으면 예외를 발생시킨다")
    void findById_should_throw_exception_when_not_exists() {
        // given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SubcategoryWeeklyStatistics not found");
    }
}
