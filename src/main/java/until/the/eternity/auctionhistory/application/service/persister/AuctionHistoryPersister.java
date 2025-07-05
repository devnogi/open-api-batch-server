package until.the.eternity.auctionhistory.application.service.persister;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepository;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.domain.service.persister.AuctionHistoryPersisterPort;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionHistoryPersister implements AuctionHistoryPersisterPort {

    private final AuctionHistoryRepository repository;
    private final OpenApiAuctionHistoryMapper mapper;
    private final AuctionHistoryDuplicateChecker duplicateChecker;

    public void saveIfNotExists(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        List<String> incomingIds =
                dtoList.stream().map(OpenApiAuctionHistoryResponse::auctionBuyId).toList();

        List<String> existingIds = duplicateChecker.findExistingIds(incomingIds);

        List<AuctionHistory> newEntities =
                dtoList.stream()
                        .filter(
                                dto ->
                                        !duplicateChecker.isDuplicate(
                                                dto.auctionBuyId(), existingIds))
                        .map(dto -> mapper.toEntity(dto, category).linkItemOptions())
                        .toList(); // MapStruct의 AfterMapping 실행 이슈로 linkItemOptions()로 처리

        if (newEntities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getSubCategory());
            return;
        }

        repository.saveAll(newEntities);
        logSummary(category, newEntities);
    }

    private void logSummary(ItemCategory category, List<AuctionHistory> entities) {
        int optionCnt =
                entities.stream()
                        .mapToInt(e -> e.getItemOptions() == null ? 0 : e.getItemOptions().size())
                        .sum();

        log.info(
                "[{}] Saved {} new auction history records (with {} options)",
                category.getSubCategory(),
                entities.size(),
                optionCnt);

        if (log.isDebugEnabled()) {
            entities.forEach(this::logEntityDebug);
        }
    }

    private void logEntityDebug(AuctionHistory history) {
        log.debug(
                "Mapped AuctionHistory: auctionBuyId={}, itemOptions={}",
                history.getAuctionBuyId(),
                history.getItemOptions() == null ? 0 : history.getItemOptions().size());
    }
}
