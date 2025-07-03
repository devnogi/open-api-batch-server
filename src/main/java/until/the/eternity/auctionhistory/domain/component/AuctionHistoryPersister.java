package until.the.eternity.auctionhistory.domain.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auctionhistory.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;
import until.the.eternity.itemoption.domain.entity.ItemOption;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
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
                        .map(dto -> entityMapper.toEntity(dto, category).linkItemOptions())
                        .toList();

        if (newEntities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getSubCategory());
            return;
        }

        // ✅ 매핑 확인용 디버깅 로그
        for (AuctionHistory history : newEntities) {
            log.debug(
                    "Mapped AuctionHistory: auctionBuyId={}, itemOptions={}",
                    history.getAuctionBuyId(),
                    history.getItemOptions() != null ? history.getItemOptions().size() : 0);

            if (history.getItemOptions() != null) {
                for (ItemOption option : history.getItemOptions()) {
                    log.debug(
                            " - ItemOption: optionType={}, auctionHistorySet={}",
                            option.getOptionType(),
                            option.getAuctionHistory() != null);
                }
            }
        }

        auctionHistoryRepository.saveAll(newEntities);

        log.info(
                "[{}] Saved {} new auction history records (with {} options)",
                category.getSubCategory(),
                newEntities.size(),
                newEntities.stream()
                        .filter(h -> h.getItemOptions() != null)
                        .mapToInt(h -> h.getItemOptions().size())
                        .sum());
    }
}
