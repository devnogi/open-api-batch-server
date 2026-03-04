package until.the.eternity.auctionrealtime.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.PriceSearchRequest;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.mapper.AuctionRealtimeMapper;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.DateAuctionExpireRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;
import until.the.eternity.common.response.PageResponseDto;

@ExtendWith(MockitoExtension.class)
class AuctionRealtimeServiceTest {

    @Mock private AuctionRealtimeItemRepositoryPort repository;
    @Mock private AuctionRealtimeMapper mapper;

    @InjectMocks private AuctionRealtimeService service;

    @Test
    @DisplayName("검색은 페이지 응답을 반환한다")
    void search_should_return_paged_response() {
        AuctionRealtimeSearchRequest request =
                new AuctionRealtimeSearchRequest(
                        "테스트 아이템",
                        false,
                        "근거리 장비",
                        "검",
                        new PriceSearchRequest(1000L, 5000L),
                        new DateAuctionExpireRequest("2026-03-01", "2026-03-05"),
                        null,
                        null,
                        null);
        Pageable pageable = PageRequest.of(0, 10);

        AuctionRealtimeItem entity = new AuctionRealtimeItem();
        AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> detailDto =
                mock(AuctionRealtimeDetailResponse.class);
        Page<AuctionRealtimeItem> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(repository.search(request, pageable)).thenReturn(entityPage);
        when(mapper.toDto(entity)).thenReturn(detailDto);

        PageResponseDto<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> result =
                service.search(request, pageable);

        assertThat(result.items()).hasSize(1).contains(detailDto);
        verify(repository).search(request, pageable);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("데이터가 없으면 findByIdOrElseThrow는 예외를 던진다")
    void findByIdOrElseThrow_should_throw_exception_when_not_found() {
        Long id = 999L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.findByIdOrElseThrow(id));
        verify(repository).findById(id);
        verifyNoInteractions(mapper);
    }
}
