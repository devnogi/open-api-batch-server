package until.the.eternity.auction.domain.component;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auction.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@RequiredArgsConstructor
@Slf4j
@Component
public class AuctionHistoryPersister {

    private final AuctionHistoryRepository auctionHistoryRepository;
    private final OpenApiAuctionHistoryMapper entityMapper;

    public void saveIfNotExists(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        List<String> incomingIds =
                dtoList.stream().map(OpenApiAuctionHistoryResponse::auctionBuyId).toList();

        List<String> existingIds = auctionHistoryRepository.findExistingIds(incomingIds);

        List<AuctionHistory> newEntities =
                dtoList.stream()
                        .filter(dto -> !existingIds.contains(dto.auctionBuyId()))
                        .map(dto -> entityMapper.toEntity(dto, category))
                        .toList();

        if (newEntities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getSubCategory());
            return;
        }

        auctionHistoryRepository.saveAll(newEntities);
        log.info(
                "[{}] Saved {} new auction history records (with {} options)",
                category.getSubCategory(),
                newEntities.size(),
                newEntities.stream().mapToInt(h -> h.getItemOptions().size()).sum());
    }
}
