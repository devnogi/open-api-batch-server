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

    public List<AuctionHistory> filterOutExisting(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        List<AuctionHistory> entities =
                mapper.toEntityList(duplicateChecker.filterExisting(dtoList), category);

        if (entities.isEmpty()) {
            log.info("[SCHEDULE] [{}] No new auction history to save", category.getSubCategory());
        } else {
            log.info(
                    "[SCHEDULE] [{}] After remove duplicate existing [{}] new auction history records left to save",
                    category.getSubCategory(),
                    entities.size());
        }

        return entities;
    }
}
