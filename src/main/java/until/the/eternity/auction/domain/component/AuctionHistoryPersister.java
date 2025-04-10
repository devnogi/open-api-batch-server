package until.the.eternity.auction.domain.component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.model.ItemOption;
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
                        .map(dto -> convertToEntity(dto, category))
                        .collect(Collectors.toList());

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

    private AuctionHistory convertToEntity(AuctionHistoryDto dto, ItemCategory category) {
        AuctionHistory auctionHistory =
                AuctionHistory.builder()
                        .itemName(dto.getItemName())
                        .itemDisplayName(dto.getItemDisplayName())
                        .itemCount(dto.getItemCount())
                        .auctionPricePerUnit(dto.getAuctionPricePerUnit())
                        .dateAuctionBuy(OffsetDateTime.parse(dto.getDateAuctionBuy()).toInstant())
                        .auctionBuyId(dto.getAuctionBuyId())
                        .itemSubCategory(category.getSubCategory())
                        .itemTopCategory(category.getTopCategory())
                        .build();

        // ItemOption도 같이 변환
        if (dto.getItemOptionDtos() != null) {
            List<ItemOption> itemOptions =
                    dto.getItemOptionDtos().stream()
                            .map(
                                    optionDto ->
                                            ItemOption.builder()
                                                    .optionType(optionDto.getOptionType())
                                                    .optionSubType(optionDto.getOptionSubType())
                                                    .optionValue(optionDto.getOptionValue())
                                                    .optionValue2(optionDto.getOptionValue2())
                                                    .optionDesc(optionDto.getOptionDesc())
                                                    .auctionHistory(auctionHistory) // 연관관계 설정
                                                    .build())
                            .collect(Collectors.toList());

            auctionHistory.setItemOptions(itemOptions);
        }

        return auctionHistory;
    }
}
