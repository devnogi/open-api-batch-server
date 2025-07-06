package until.the.eternity.auctionhistory.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;

import java.util.Collection;
import java.util.List;

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

    // TODO: 카테고리 별 마지막 거래 일자 이후의 데이터를 중복으로 처리하는 코드 작성

    public List<String> findExistingIds(List<String> incomingIds) {
        return repository.findExistingIds(incomingIds);
    }

    public boolean isDuplicate(String id, List<String> existingIds) {
        return existingIds.contains(id);
    }
}
