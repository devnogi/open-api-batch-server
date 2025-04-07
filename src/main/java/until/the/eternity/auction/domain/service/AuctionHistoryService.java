package until.the.eternity.auction.domain.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.dto.AuctionHistoryResponse;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionHistoryService {

    private final WebClient webClient;
    private final AuctionHistoryRepository auctionHistoryRepository; // JPA Repo

    @Value("${openapi.nexon.api-key}")
    private String nexonApiKey;

    @Scheduled(cron = "0 */1 * * * *")
    public void fetchAndSaveAuctionHistoryAll() {
        for (ItemCategory category : ItemCategory.values()) {
            fetchAndSaveAuctionHistory(category);
        }
    }

    public void fetchAndSaveAuctionHistory(ItemCategory itemCategory) {

        try {
            AuctionHistoryResponse response =
                    webClient
                            .get()
                            .uri(
                                    uriBuilder ->
                                            uriBuilder
                                                    .path("/auction/history")
                                                    .queryParam(
                                                            "auction_item_category",
                                                            itemCategory.getItemName())
                                                    .build())
                            .header("x-nxopen-api-key", nexonApiKey)
                            .header("accept", "application/json")
                            .retrieve()
                            .bodyToMono(AuctionHistoryResponse.class)
                            .block();

            try {
                if (response != null && response.getAuction_history() != null) {
                    log.info("✅ Auction history save start");
                    List<AuctionHistory> entities =
                            response.getAuction_history().stream()
                                    .map(this::convertToEntity)
                                    .collect(Collectors.toList());

                    auctionHistoryRepository.saveAll(entities);
                    log.info("✅ Auction history 저장 완료 - {}건", entities.size());
                } else {
                    log.info("✅ Auction history is null");
                }
            } catch (Exception e) {
                log.error("Auction history save fail", e.fillInStackTrace());
            }

        } catch (Exception e) {
            log.error("❌ API 호출 실패", e);
        }
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
