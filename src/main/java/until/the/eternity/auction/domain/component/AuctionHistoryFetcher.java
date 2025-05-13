package until.the.eternity.auction.domain.component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.dto.AuctionHistoryResponse;
import until.the.eternity.auction.domain.repository.AuctionHistoryRepository;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcher {

    private final WebClient webClient;
    private final AuctionHistoryRepository auctionHistoryRepository;

    @Value("${openapi.nexon.api-key}")
    private String nexonApiKey;

    public List<AuctionHistoryDto> fetch(ItemCategory category) {
        List<AuctionHistoryDto> result = new ArrayList<>();
        String cursor = null;

        do {
            AuctionHistoryResponse response = fetchFromApi(category, cursor);
            if (response == null || response.getAuction_history() == null) break;

            List<AuctionHistoryDto> currentBatch = response.getAuction_history();
            if (containsExistingIds(currentBatch)) break;

            result.addAll(currentBatch);
            cursor = response.getNext_cursor();
        } while (cursor != null);

        return result;
    }

    private AuctionHistoryResponse fetchFromApi(ItemCategory category, String cursor) {
        try {
            return webClient
                    .get()
                    .uri(
                            uriBuilder -> {
                                uriBuilder
                                        .path("/auction/history")
                                        .queryParam(
                                                "auction_item_category", category.getSubCategory());
                                if (cursor != null) {
                                    uriBuilder.queryParam("cursor", cursor);
                                }
                                return uriBuilder.build();
                            })
                    .header("x-nxopen-api-key", nexonApiKey)
                    .header("accept", "application/json")
                    .retrieve()
                    .bodyToMono(AuctionHistoryResponse.class)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2)) // 🔁 최대 3번 재시도, 2초 간격 (지수 백오프)
                                    .filter(this::isRetryableException)) // 재시도 조건
                    .onErrorResume(
                            throwable -> {
                                log.warn(
                                        "Failed to fetch auction history [category={} cursor={}]: {}",
                                        category,
                                        cursor,
                                        throwable.toString());
                                return Mono.empty(); // graceful fail
                            })
                    .block();
        } catch (Exception ex) {
            log.error("Unexpected exception during auction history fetch", ex);
            return null;
        }
    }

    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof WebClientResponseException
                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

    private boolean containsExistingIds(List<AuctionHistoryDto> dtos) {
        List<String> ids =
                dtos.stream().map(AuctionHistoryDto::getAuctionBuyId).collect(Collectors.toList());
        return auctionHistoryRepository.existsByAuctionBuyIdIn(ids);
    }
}
