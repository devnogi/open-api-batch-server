package until.the.eternity.auctionhistory.domain.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Component
@RequiredArgsConstructor
public class AuctionHistoryDuplicateChecker {

    private final AuctionHistoryRepositoryPort repository;

    /**
     * 주어진 DTO 컬렉션 안에 이미 저장된 auctionBuyId 가 있는지 추후 거대한 뿔피리 및 실시간 거래 정보 API를 활용하면 공통 component로 변경 고려
     */
    public boolean hasDuplicate(Collection<OpenApiAuctionHistoryResponse> dtos) {
        if (dtos.isEmpty()) return false;
        List<String> ids = dtos.stream().map(OpenApiAuctionHistoryResponse::auctionBuyId).toList();
        return repository.existsByAuctionBuyIdIn(ids); // 이 코드가 중복여부만 판단하는게 아니라, 어떤 데이터가 중복인지 알아야돼
    }

    public List<OpenApiAuctionHistoryResponse> filterExisting(List<OpenApiAuctionHistoryResponse> dtos) {
        LocalDateTime latestDate = repository.findLatestDateAuctionBuyBySubCategory(ItemCategory.findBySubCategory(dtos.getFirst().itemSubCategory()))
                .orElseThrow(() -> new IllegalStateException("No auction history found")); // TODO: Custom 에러로 변경
        return dtos
                .stream()
                .filter(dto ->
                        dto.dateAuctionBuy().isAfter(latestDate)).toList();
    }

    // 기존 데이터와 중복 데이터 검증 로직
    // 1. ID 대조 그런데 이러면....
    // 2. 시간대별로 정렬이 잘되어 있다고 하면 마지막 시간대 이후인 아이템으로 검증하면 좋을 거 같긴한데
    // 3. 아니면 복합적으로 일정 시간 대 이후로 1차 필터링, 그 다음에 ID로 2차 필터링...?

    // TODO: 카테고리 별 마지막 거래 일자 이후의 데이터를 중복으로 처리하는 코드 작성

    public List<String> findExistingIds(List<String> incomingIds) {
        return repository.findExistingIds(incomingIds);
    }

    public boolean isDuplicate(String id, List<String> existingIds) {
        return existingIds.contains(id);
    }
}
