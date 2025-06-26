package until.the.eternity.auction.domain.component;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auction.domain.mapper.AuctionHistoryMapper;
import until.the.eternity.auction.domain.mapper.OpenApiAuctionHistoryAssembler;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@RequiredArgsConstructor
@Slf4j
@Component
public class AuctionHistoryPersister {

    private final AuctionHistoryRepository auctionHistoryRepository;
    private final AuctionHistoryMapper auctionHistoryMapper;
    private final OpenApiAuctionHistoryAssembler assembler;

    public void saveIfNotExists(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        List<String> incomingIds =
                dtoList.stream().map(OpenApiAuctionHistoryResponse::auctionBuyId).toList();

        List<String> existingIds =
                auctionHistoryRepository.findAllByAuctionBuyIdIn(incomingIds).stream()
                        .map(AuctionHistory::getAuctionBuyId)
                        .toList();

        List<AuctionHistory> newEntities =
                dtoList.stream()
                        .filter(dto -> !existingIds.contains(dto.auctionBuyId()))
                        .map(
                                dto -> {
                                    var detail = assembler.toDetailResponse(dto, category);
                                    return auctionHistoryMapper.toEntity(detail);
                                })
                        .toList();

        if (newEntities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getSubCategory());
            return;
        }

        auctionHistoryRepository.saveAll(newEntities);
        log.info(
                "[{}] Saved {} new auction history records",
                category.getSubCategory(),
                newEntities.size());
    }
}
