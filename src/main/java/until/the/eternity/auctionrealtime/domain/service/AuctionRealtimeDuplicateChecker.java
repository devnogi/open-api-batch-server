package until.the.eternity.auctionrealtime.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

/**
 * 실시간 경매장 데이터의 중복 체크 로직.
 *
 * <p>중복 판단 기준:
 *
 * <ul>
 *   <li>date_auction_expire < latestDate → 중복 (과거 데이터)
 *   <li>date_auction_expire == latestDate → 해당 시간대 데이터 삭제 후 재저장 필요
 *   <li>date_auction_expire > latestDate → 신규 데이터
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRealtimeDuplicateChecker {

    private final AuctionRealtimeItemRepositoryPort repository;

    /**
     * 중복 체크 결과.
     *
     * @param isDuplicate 중복 여부
     * @param hasEqualDate 동일 날짜 데이터 존재 여부 (삭제 후 재저장 필요)
     * @param duplicateIndex 중복이 발생한 첫 번째 인덱스 (-1이면 중복 없음)
     * @param latestDate DB의 최신 date_auction_expire
     */
    public record DuplicateCheckResult(
            boolean isDuplicate, boolean hasEqualDate, int duplicateIndex, Instant latestDate) {

        public static DuplicateCheckResult noDuplicate() {
            return new DuplicateCheckResult(false, false, -1, null);
        }

        public static DuplicateCheckResult noDuplicateWithLatestDate(Instant latestDate) {
            return new DuplicateCheckResult(false, false, -1, latestDate);
        }

        public static DuplicateCheckResult duplicateFound(int index, Instant latestDate) {
            return new DuplicateCheckResult(true, false, index, latestDate);
        }

        public static DuplicateCheckResult equalDateFound(int index, Instant latestDate) {
            return new DuplicateCheckResult(true, true, index, latestDate);
        }
    }

    /**
     * 배치에서 중복 또는 동일 날짜 데이터를 체크한다.
     *
     * @param batch 검사할 배치 데이터
     * @param category 아이템 카테고리
     * @return 중복 체크 결과
     */
    public DuplicateCheckResult checkDuplicateInBatch(
            List<OpenApiAuctionRealtimeResponse> batch, ItemCategory category) {
        if (batch == null || batch.isEmpty()) {
            return DuplicateCheckResult.noDuplicate();
        }

        Optional<Instant> latestDateOpt =
                repository.findLatestDateAuctionExpireBySubCategory(category);

        if (latestDateOpt.isEmpty()) {
            // DB에 데이터가 없으면 모두 신규
            return DuplicateCheckResult.noDuplicate();
        }

        Instant latestDate = latestDateOpt.get();

        for (int i = 0; i < batch.size(); i++) {
            OpenApiAuctionRealtimeResponse dto = batch.get(i);
            Instant dtoDate = dto.dateAuctionExpire();

            if (dtoDate == null) {
                continue;
            }

            if (dtoDate.isBefore(latestDate)) {
                // 과거 데이터 → 중복
                return DuplicateCheckResult.duplicateFound(i, latestDate);
            }

            if (dtoDate.equals(latestDate)) {
                // 동일 날짜 → 삭제 후 재저장 필요
                return DuplicateCheckResult.equalDateFound(i, latestDate);
            }
        }

        // 모든 데이터가 latestDate보다 이후
        return DuplicateCheckResult.noDuplicateWithLatestDate(latestDate);
    }

    /**
     * API 응답 데이터를 필터링하여 저장 가능한 데이터만 반환한다.
     *
     * @param dtos 필터링할 DTO 리스트
     * @param latestDate DB의 최신 date_auction_expire (null이면 모두 저장)
     * @return 저장할 데이터 리스트
     */
    public List<OpenApiAuctionRealtimeResponse> filterForSave(
            List<OpenApiAuctionRealtimeResponse> dtos, Instant latestDate) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        if (latestDate == null) {
            return dtos;
        }

        return dtos.stream()
                .filter(
                        dto -> {
                            Instant dtoDate = dto.dateAuctionExpire();
                            // latestDate 이후 또는 동일한 날짜만 저장
                            return dtoDate != null && !dtoDate.isBefore(latestDate);
                        })
                .toList();
    }
}
