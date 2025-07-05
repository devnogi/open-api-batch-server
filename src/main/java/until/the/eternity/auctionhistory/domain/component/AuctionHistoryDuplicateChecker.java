package until.the.eternity.auctionhistory.domain.component;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auctionhistory.repository.AuctionHistoryRepository;

@Component
@RequiredArgsConstructor
public class AuctionHistoryDuplicateChecker {

    private final AuctionHistoryRepository auctionHistoryRepository;

    /**
     * 주어진 DTO 컬렉션 안에 이미 저장된 auctionBuyId 가 있는지
     * 추후 거대한 뿔피리 및 실시간 거래 정보 API를 활용하면 공통 component로 변경 고려
     */
    public boolean hasDuplicate(Collection<OpenApiAuctionHistoryResponse> dtos) {
        if (dtos.isEmpty()) return false;
        List<String> ids = dtos.stream().map(OpenApiAuctionHistoryResponse::auctionBuyId).toList();
        return auctionHistoryRepository.existsByAuctionBuyIdIn(ids);
    }
}
