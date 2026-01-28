package until.the.eternity.auctionrealtime.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionrealtime.domain.mapper.AuctionRealtimeMapper;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.common.response.PageResponseDto;

import java.time.Instant;
import java.util.List;

/** 실시간 경매장 데이터 Service. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionRealtimeService {

    private final AuctionRealtimeItemRepositoryPort repository;
    private final AuctionRealtimeMapper mapper;

    /**
     * 실시간 경매장 아이템을 검색한다.
     *
     * @param requestDto 검색 조건
     * @param pageable 페이지 정보
     * @return 검색 결과
     */
    @Transactional(readOnly = true)
    public PageResponseDto<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> search(
            AuctionRealtimeSearchRequest requestDto, Pageable pageable) {

        Page<AuctionRealtimeItem> page = repository.search(requestDto, pageable);
        Page<AuctionRealtimeDetailResponse<RealtimeItemOptionResponse>> dtoPage =
                page.map(mapper::toDto);
        return PageResponseDto.of(dtoPage);
    }

    /**
     * ID로 실시간 경매장 아이템을 조회한다.
     *
     * @param id 아이템 ID
     * @return 아이템 상세 정보
     */
    @Transactional(readOnly = true)
    public AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> findByIdOrElseThrow(Long id) {
        AuctionRealtimeItem item =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "AuctionRealtimeItem not found: " + id));
        return mapper.toDto(item);
    }

    /**
     * 해당 카테고리 & 동일 date_auction_expire 레코드 삭제 후 새 엔티티들을 저장한다.
     *
     * @param category 아이템 카테고리
     * @param dateAuctionExpire 삭제할 date_auction_expire
     * @param entities 저장할 엔티티 리스트
     */
    @Transactional
    public void deleteAndSave(
            ItemCategory category, Instant dateAuctionExpire, List<AuctionRealtimeItem> entities) {

        // 동일 date_auction_expire 레코드 삭제
        int deleted =
                repository.deleteBySubCategoryAndDateAuctionExpire(category, dateAuctionExpire);
        log.info(
                "[REALTIME] [{}] Deleted {} records with date_auction_expire={}",
                category.getSubCategory(),
                deleted,
                dateAuctionExpire);

        // 새 엔티티 저장
        repository.saveAll(entities);
        log.info(
                "[REALTIME] [{}] Saved {} new auction realtime items",
                category.getSubCategory(),
                entities.size());
    }

    /**
     * 엔티티들을 저장한다. (삭제 없이)
     *
     * @param entities 저장할 엔티티 리스트
     */
    @Transactional
    public void saveAll(List<AuctionRealtimeItem> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        repository.saveAll(entities);
        log.debug("[REALTIME] Saved {} auction realtime items", entities.size());
    }

    /**
     * 만료된 아이템을 삭제한다.
     *
     * @param now 현재 시각
     * @return 삭제된 레코드 수
     */
    @Transactional
    public int deleteExpiredItems(Instant now) {
        int deleted = repository.deleteExpiredItems(now);
        log.info("[REALTIME] Deleted {} expired auction realtime items", deleted);
        return deleted;
    }
}
