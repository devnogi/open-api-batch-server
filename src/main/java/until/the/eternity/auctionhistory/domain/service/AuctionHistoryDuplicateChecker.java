package until.the.eternity.auctionhistory.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort.LatestDateWithIds;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryDuplicateChecker {

    private final AuctionHistoryRepositoryPort repository;

    /**
     * 배치에서 첫 번째 중복 데이터의 인덱스를 반환합니다.
     *
     * <p>중복 판정 로직:
     *
     * <ul>
     *   <li>date_auction_buy < latestDate → 중복 (과거 데이터)
     *   <li>date_auction_buy == latestDate → auctionBuyId가 DB에 존재하면 중복
     *   <li>date_auction_buy > latestDate → 신규 데이터
     * </ul>
     *
     * @param batch 검사할 배치 데이터
     * @param category 아이템 카테고리
     * @return 첫 번째 중복 인덱스, 중복이 없으면 empty
     */
    public OptionalInt checkDuplicateInBatch(
            List<OpenApiAuctionHistoryResponse> batch, ItemCategory category) {
        if (batch.isEmpty()) {
            return OptionalInt.empty();
        }

        var latestInfo = repository.findLatestDateWithIdsBySubCategory(category);

        if (latestInfo.isEmpty()) {
            return OptionalInt.empty();
        }

        LatestDateWithIds info = latestInfo.get();
        Instant latestDate = info.latestDate();
        Set<String> existingIds = info.existingIds();

        for (int i = 0; i < batch.size(); i++) {
            OpenApiAuctionHistoryResponse dto = batch.get(i);
            if (isDuplicate(dto, latestDate, existingIds)) {
                return OptionalInt.of(i);
            }
        }

        return OptionalInt.empty();
    }

    /**
     * DTO 리스트에서 이미 DB에 존재하는 데이터를 필터링합니다.
     *
     * @param dtos 필터링할 DTO 리스트
     * @param category 아이템 카테고리
     * @return 새로운 데이터만 포함된 리스트
     */
    public List<OpenApiAuctionHistoryResponse> filterExisting(
            List<OpenApiAuctionHistoryResponse> dtos, ItemCategory category) {
        if (dtos.isEmpty()) {
            return dtos;
        }

        var latestInfo = repository.findLatestDateWithIdsBySubCategory(category);

        if (latestInfo.isEmpty()) {
            return dtos;
        }

        LatestDateWithIds info = latestInfo.get();
        Instant latestDate = info.latestDate();
        Set<String> existingIds = info.existingIds();

        return dtos.stream().filter(dto -> !isDuplicate(dto, latestDate, existingIds)).toList();
    }

    private boolean isDuplicate(
            OpenApiAuctionHistoryResponse dto, Instant latestDate, Set<String> existingIds) {
        Instant dtoDate = dto.dateAuctionBuy();

        if (dtoDate.isBefore(latestDate)) {
            return true;
        }

        if (dtoDate.equals(latestDate)) {
            return existingIds.contains(dto.auctionBuyId());
        }

        return false;
    }
}
