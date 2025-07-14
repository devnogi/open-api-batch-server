package until.the.eternity.auctionhistory.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryDuplicateChecker {

    private final AuctionHistoryRepositoryPort repository;

    /**
     * 주어진 DTO 컬렉션 안에 이미 저장된 auctionBuyId 가 있는지 추후 거대한 뿔피리 및 실시간 거래 정보 API를 활용하면 공통 component로 변경 고려
     */
    public boolean hasDuplicate(OpenApiAuctionHistoryResponse lastDto) {
        Instant latestDate = getLatestAuctionDateOrMin(lastDto);

        return lastDto.dateAuctionBuy().isAfter(latestDate);
    }

    // TODO: 로직 변경 후 점검 중인데, 뭔가 문제가 있는 거 같음

    public List<OpenApiAuctionHistoryResponse> filterExisting(
            List<OpenApiAuctionHistoryResponse> dtos) {
        Instant latestDate = getLatestAuctionDateOrMin(dtos.getFirst());
        return dtos.stream().filter(dto -> dto.dateAuctionBuy().isAfter(latestDate)).toList();
    }

    private Instant getLatestAuctionDateOrMin(OpenApiAuctionHistoryResponse dto) {
        return repository
                .findLatestDateAuctionBuyBySubCategory(
                        ItemCategory.findBySubCategory(dto.itemSubCategory()))
                .orElse(Instant.MIN); // 기존에 카테고리가 없는 아이템이면 무조건 저장하기 위해서 Instant.MIN 반환
    }
}
