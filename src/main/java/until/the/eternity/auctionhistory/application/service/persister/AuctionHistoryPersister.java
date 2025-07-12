package until.the.eternity.auctionhistory.application.service.persister;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.domain.service.persister.AuctionHistoryPersisterPort;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuctionHistoryPersister implements AuctionHistoryPersisterPort {

    private final AuctionHistoryRepositoryPort repository;
    private final OpenApiAuctionHistoryMapper mapper;
    private final AuctionHistoryDuplicateChecker duplicateChecker;

    public void saveIfNotExists(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        List<AuctionHistory> entities =
                mapper.toEntityList(duplicateChecker.filterExisting(dtoList, category), category);

        if (entities.isEmpty()) {
            log.info("[{}] No new auction history to save", category.getSubCategory());
            return;
        }

        repository.saveAll(entities);
        logSummary(category, entities);
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
    }
}
