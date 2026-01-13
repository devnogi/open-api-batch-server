package until.the.eternity.statistics.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.statistics.domain.entity.weekly.ItemWeeklyStatistics;
import until.the.eternity.statistics.domain.mapper.ItemWeeklyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemWeeklyStatisticsResponse;
import until.the.eternity.statistics.repository.weekly.ItemWeeklyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class ItemWeeklyStatisticsServiceTest {

    @Mock private ItemWeeklyStatisticsRepository repository;
    @Mock private ItemWeeklyStatisticsMapper mapper;

    @InjectMocks private ItemWeeklyStatisticsService service;

    @Test
    @DisplayName("findAll은 페이징된 주간 통계 목록을 반환한다")
    void findAll_should_return_paged_statistics() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ItemWeeklyStatistics entity =
                ItemWeeklyStatistics.builder()
                        .id(1L)
                        .itemName("Test Item")
                        .year(2025)
                        .weekNumber(27)
                        .weekStartDate(LocalDate.of(2025, 7, 1))
                        .minPrice(100000L)
                        .maxPrice(150000L)
                        .avgPrice(new BigDecimal("125000.00"))
                        .totalVolume(35000000L)
                        .totalQuantity(700L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        ItemWeeklyStatisticsResponse response =
                new ItemWeeklyStatisticsResponse(
                        1L,
                        "Test Item",
                        2025,
                        27,
                        LocalDate.of(2025, 7, 1),
                        100000L,
                        150000L,
                        new BigDecimal("125000.00"),
                        35000000L,
                        700L,
                        LocalDateTime.now(),
                        LocalDateTime.now());

        Page<ItemWeeklyStatistics> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toDto(entity)).thenReturn(response);

        // when
        PageResponseDto<ItemWeeklyStatisticsResponse> result = service.findAll(pageable);

        // then
        assertThat(result.items()).hasSize(1).contains(response);
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("findById는 데이터가 없으면 예외를 발생시킨다")
    void findById_should_throw_exception_when_not_exists() {
        // given
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ItemWeeklyStatistics not found");
    }
}
