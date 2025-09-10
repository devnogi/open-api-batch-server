package until.the.eternity.auctionhistory.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.AuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.common.request.PageRequestDto;
import until.the.eternity.common.response.PageResponseDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final AuctionHistoryRepositoryPort repository;
    private final AuctionHistoryMapper mapper;

    @Transactional(readOnly = true)
    public PageResponseDto<AuctionHistoryDetailResponse<ItemOptionResponse>> search(
            AuctionHistorySearchRequest requestDto, PageRequestDto pageRequestDto) {

        Page<AuctionHistory> page = repository.search(requestDto, pageRequestDto.toPageable());
        Page<AuctionHistoryDetailResponse<ItemOptionResponse>> dtoPage = page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    @Transactional(readOnly = true)
    public AuctionHistoryDetailResponse<ItemOptionResponse> findByIdOrElseThrow(String id) {
        AuctionHistory auctionHistory =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "AuctionHistory not found: " + id));
        return mapper.toDto(auctionHistory);
    }

    @Transactional
    public void saveAll(List<AuctionHistory> entities) {
        repository.saveAll(entities);
    }
}
