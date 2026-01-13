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
import until.the.eternity.statistics.domain.entity.daily.SubcategoryDailyStatistics;
import until.the.eternity.statistics.domain.mapper.SubcategoryDailyStatisticsMapper;
import until.the.eternity.statistics.interfaces.rest.dto.response.SubcategoryDailyStatisticsResponse;
import until.the.eternity.statistics.repository.daily.SubcategoryDailyStatisticsRepository;

@ExtendWith(MockitoExtension.class)
class SubcategoryDailyStatisticsServiceTest {

    @Mock private SubcategoryDailyStatisticsRepository repository;
    @Mock private SubcategoryDailyStatisticsMapper mapper;

    @InjectMocks private SubcategoryDailyStatisticsService service;

    @Test
    @DisplayName("findAll은 페이징된 통계 목록을 반환한다")
    void findAll_should_return_paged_statistics() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        SubcategoryDailyStatistics entity =
                SubcategoryDailyStatistics.builder()
                        .id(1L)
                        .itemSubCategory("한손검")
                        .dateAuctionBuy(LocalDate.of(2025, 7, 1))
                        .minPrice(100000L)
                        .maxPrice(150000L)
                        .avgPrice(new BigDecimal("125000.00"))
                        .totalVolume(50000000L)
                        .totalQuantity(1000L)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        SubcategoryDailyStatisticsResponse response =
                new SubcategoryDailyStatisticsResponse(
                        1L,
                        "한손검",
                        LocalDate.of(2025, 7, 1),
                        100000L,
                        150000L,
                        new BigDecimal("125000.00"),
                        50000000L,
                        1000L,
                        LocalDateTime.now(),
                        LocalDateTime.now());

        Page<SubcategoryDailyStatistics> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(entityPage);
        when(mapper.toDto(entity)).thenReturn(response);

        // when
        PageResponseDto<SubcategoryDailyStatisticsResponse> result = service.findAll(pageable);

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
                .hasMessageContaining("SubcategoryDailyStatistics not found");
    }
}
