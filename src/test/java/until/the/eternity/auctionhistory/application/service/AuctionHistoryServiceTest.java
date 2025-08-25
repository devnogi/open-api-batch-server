package until.the.eternity.auctionhistory.application.service;

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
import until.the.eternity.auctionhistory.application.service.persister.AuctionHistoryPersister;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.AuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.domain.service.fetcher.AuctionHistoryFetcherPort;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryServiceTest {

    @Mock private AuctionHistoryRepositoryPort repositoryPort;
    @Mock private AuctionHistoryFetcherPort fetcherPort;
    @Mock private AuctionHistoryPersister persister;
    @Mock private AuctionHistoryMapper mapper;

    @InjectMocks private AuctionHistoryService service;

    @Test
    @DisplayName("검색은 무조건 페이지를 반환한다")
    void search_should_return_paged_response() {
        // given
        AuctionHistorySearchRequest searchRequest = new AuctionHistorySearchRequest();
        PageRequestDto pageRequestDto = mock(PageRequestDto.class);
        Pageable pageable = PageRequest.of(0, 10);
        when(pageRequestDto.toPageable()).thenReturn(pageable);

        AuctionHistory entity = new AuctionHistory();
        AuctionHistoryDetailResponse<ItemOptionResponse> detailDto =
                mock(AuctionHistoryDetailResponse.class);
        Page<AuctionHistory> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(repositoryPort.search(searchRequest, pageable)).thenReturn(entityPage);
        when(mapper.toDto(entity)).thenReturn(detailDto);

        // when
        PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> result =
                service.search(searchRequest, pageRequestDto);

        // then
        assertThat(result.items()).hasSize(1).contains(detailDto);
        verify(repositoryPort).search(searchRequest, pageable);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("데이터가 존재하면 findByIdOrElseThrow는 dto를 반환한다")
    void findByIdOrElseThrow_should_return_dto_when_entity_exists() {
        // given
        Long id = 1L;
        AuctionHistory entity = new AuctionHistory();
        AuctionHistoryDetailResponse<ItemOptionResponse> dto =
                mock(AuctionHistoryDetailResponse.class);

        when(repositoryPort.findById(id)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        // when
        AuctionHistoryDetailResponse<ItemOptionResponse> result = service.findByIdOrElseThrow(id);

        // then
        assertThat(result).isEqualTo(dto);
        verify(repositoryPort).findById(id);
        verify(mapper).toDto(entity);
    }

    @Test
    @DisplayName("검색 경매장 거래 내역 ID가 존재하지 않으면 예외처리를 한다")
    void findByIdOrElseThrow_should_throw_exception_when_not_found() {
        // given
        Long id = 999L;
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        // expect
        assertThrows(IllegalArgumentException.class, () -> service.findByIdOrElseThrow(id));
        verify(repositoryPort).findById(id);
        verifyNoInteractions(mapper);
    }

    //    @Test
    //    @DisplayName("경매장 fetch 및 Save 로직은 fetcher와 persister에 위임한다")
    //    void fetchAndSaveAuctionHistory_should_delegate_to_fetcher_and_persister() {
    //        // given
    //        ItemCategory category = ItemCategory.ETC;
    //        List<OpenApiAuctionHistoryResponse> fetchedDtoList =
    //                List.of(mock(OpenApiAuctionHistoryResponse.class));
    //
    //        when(fetcherPort.fetch(category)).thenReturn(fetchedDtoList);
    //
    //        // when
    //        service.fetchAndSaveAuctionHistory(category);
    //
    //        // then
    //        verify(fetcherPort).fetch(category);
    //        verify(persister).saveIfNotExists(fetchedDtoList, category);
    //    }
}
