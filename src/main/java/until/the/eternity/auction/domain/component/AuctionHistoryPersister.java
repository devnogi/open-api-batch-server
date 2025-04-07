package until.the.eternity.auction.domain.component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryPersister {

    private final AuctionHistoryRepository auctionHistoryRepository;

    public void saveIfNotExists(List<AuctionHistoryDto> dtoList, ItemCategory category) {
        List<String> incomingIds =
                dtoList.stream()
                        .map(AuctionHistoryDto::getAuctionBuyId)
                        .collect(Collectors.toList());

        List<String> existingIds =
                auctionHistoryRepository.findAllByAuctionBuyIdIn(incomingIds).stream()
                        .map(AuctionHistory::getAuctionBuyId)
                        .collect(Collectors.toList());

        List<AuctionHistory> newEntities =
                dtoList.stream()
                        .filter(dto -> !existingIds.contains(dto.getAuctionBuyId()))
                        .map(this::convertToEntity)
                        .collect(Collectors.toList());

        if (newEntities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getItemName());
            return;
        }

        auctionHistoryRepository.saveAll(newEntities);
        log.info(
                "[{}] Saved {} new auction history records",
                category.getItemName(),
                newEntities.size());
    }

    private AuctionHistory convertToEntity(AuctionHistoryDto dto) {
        return AuctionHistory.builder()
                .itemName(dto.getItemName())
                .itemDisplayName(dto.getItemDisplayName())
                .itemCount(dto.getItemCount())
                .auctionPricePerUnit(dto.getAuctionPricePerUnit())
                .dateAuctionBuy(OffsetDateTime.parse(dto.getDateAuctionBuy()).toInstant())
                .auctionBuyId(dto.getAuctionBuyId())
                .build();
    }
}
