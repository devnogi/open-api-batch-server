package until.the.eternity.auction.domain.component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.model.ItemOption;
import until.the.eternity.auction.domain.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryPersister {

    private final AuctionHistoryRepository auctionHistoryRepository;

    /** 외부 API 응답(record)을 받아 DB에 중복 없이 저장 */
    public void saveIfNotExists(
            List<OpenApiAuctionHistoryResponse> dtoList, ItemCategory category) {

        // 들어온 ID 목록
        List<String> incomingIds =
                dtoList.stream()
                        .map(OpenApiAuctionHistoryResponse::auctionBuyId)
                        .collect(Collectors.toList());

        // 이미 DB에 있는 ID 조회
        List<String> existingIds =
                auctionHistoryRepository.findAllByAuctionBuyIdIn(incomingIds).stream()
                        .map(AuctionHistory::getAuctionBuyId)
                        .toList();

        // 신규만 추려서 엔티티 변환
        List<AuctionHistory> newEntities =
                dtoList.stream()
                        .filter(dto -> !existingIds.contains(dto.auctionBuyId()))
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

    /** record → JPA 엔티티 변환 */
    private AuctionHistory convertToEntity(
            OpenApiAuctionHistoryResponse dto, ItemCategory category) {

        AuctionHistory auctionHistory =
                AuctionHistory.builder()
                        .itemName(dto.itemName())
                        .itemDisplayName(dto.itemDisplayName())
                        .itemCount(dto.itemCount())
                        .auctionPricePerUnit(dto.auctionPricePerUnit())
                        .dateAuctionBuy(OffsetDateTime.parse(dto.dateAuctionBuy()).toInstant())
                        .auctionBuyId(dto.auctionBuyId())
                        .itemSubCategory(category.getSubCategory())
                        .itemTopCategory(category.getTopCategory())
                        .build();

        // 옵션 정보 매핑
        if (dto.openApiItemOptionResponses() != null) {
            List<ItemOption> itemOptions =
                    dto.openApiItemOptionResponses().stream()
                            .map(
                                    opt ->
                                            ItemOption.builder()
                                                    .optionType(opt.optionType())
                                                    .optionSubType(opt.optionSubType())
                                                    .optionValue(opt.optionValue())
                                                    .optionValue2(opt.optionValue2())
                                                    .optionDesc(opt.optionDesc())
                                                    .auctionHistory(auctionHistory)
                                                    .build())
                            .collect(Collectors.toList());

            auctionHistory.setItemOptions(itemOptions);
        }

        return auctionHistory;
    }
}
