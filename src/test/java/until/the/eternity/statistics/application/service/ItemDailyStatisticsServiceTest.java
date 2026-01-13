package until.the.eternity.statistics.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
import until.the.eternity.statistics.domain.entity.daily.ItemDailyStatistics;
import until.the.eternity.statistics.domain.mapper.ItemDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.ItemDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.ItemDailyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class ItemDailyStatisticsServiceTest {

    @Mock private ItemDailyStatisticsRepository repository;
    @Mock private ItemDailyStatisticsMapper mapper;

    @InjectMocks private ItemDailyStatisticsService service;

    @Test
    @DisplayName("findAll은 페이징된 통계 목록을 반환한다")
    void findAll_should_return_paged_statistics() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ItemDailyStatistics entity = createMockEntity();
        ItemDailyStatisticsResponse response = createMockResponse();
        Page<ItemDailyStatistics> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toDto(entity)).thenReturn(response);

        // when
        PageResponseDto<ItemDailyStatisticsResponse> result = service.findAll(pageable);

        // then
        assertThat(result.items()).hasSize(1).contains(response);
        assertThat(result.meta().totalElements()).isEqualTo(1);
        verify(repository).findAll(pageable);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("findById는 ID에 해당하는 통계를 반환한다")
    void findById_should_return_statistics_when_exists() {
        // given
        Long id = 1L;
        ItemDailyStatistics entity = createMockEntity();
        ItemDailyStatisticsResponse response = createMockResponse();

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(response);

        // when
        ItemDailyStatisticsResponse result = service.findById(id);

        // then
        assertThat(result).isEqualTo(response);
        verify(repository).findById(id);
        verify(mapper).toDto(entity);
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
                .hasMessageContaining("ItemDailyStatistics not found");

        verify(repository).findById(id);
        verify(mapper, never()).toDto(any());
    }

    private ItemDailyStatistics createMockEntity() {
        return ItemDailyStatistics.builder()
                .id(1L)
                .itemName("Test Item")
                .dateAuctionBuy(LocalDate.of(2025, 7, 1))
                .minPrice(100000L)
                .maxPrice(150000L)
                .avgPrice(new BigDecimal("125000.00"))
                .totalVolume(5000000L)
                .totalQuantity(100L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private ItemDailyStatisticsResponse createMockResponse() {
        return new ItemDailyStatisticsResponse(
                1L,
                "Test Item",
                LocalDate.of(2025, 7, 1),
                100000L,
                150000L,
                new BigDecimal("125000.00"),
                5000000L,
                100L,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
