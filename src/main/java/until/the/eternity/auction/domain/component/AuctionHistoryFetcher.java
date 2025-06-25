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
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryListResponse;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryResponse;
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

    /** 외부 OPEN API에서 경매 히스토리를 가져온 뒤, 이미 저장된 ID가 나오면 중단 */
    public List<OpenApiAuctionHistoryResponse> fetch(ItemCategory category) {
        List<OpenApiAuctionHistoryResponse> result = new ArrayList<>();
        String cursor = null;

        do {
            OpenApiAuctionHistoryListResponse response = fetchFromApi(category, cursor);
            if (response == null || response.auctionHistory() == null) break;

            List<OpenApiAuctionHistoryResponse> currentBatch = response.auctionHistory();
            if (containsExistingIds(currentBatch)) break;

            result.addAll(currentBatch);
            cursor = response.nextCursor();
        } while (cursor != null);

        return result;
    }

    /** 실제 OPEN API 호출부 */
    private OpenApiAuctionHistoryListResponse fetchFromApi(ItemCategory category, String cursor) {
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
                    .bodyToMono(OpenApiAuctionHistoryListResponse.class)
                    .retryWhen(
                            Retry.backoff(3, Duration.ofSeconds(2))
                                    .filter(this::isRetryableException))
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

    /** 5xx 에러일 때만 재시도 */
    private boolean isRetryableException(Throwable throwable) {
        return throwable instanceof WebClientResponseException
                && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

    /** 이미 DB에 존재하는 auctionBuyId가 포함돼 있는지 검사 */
    private boolean containsExistingIds(List<OpenApiAuctionHistoryResponse> dtos) {
        List<String> ids =
                dtos.stream()
                        .map(OpenApiAuctionHistoryResponse::auctionBuyId)
                        .collect(Collectors.toList());
        return auctionHistoryRepository.existsByAuctionBuyIdIn(ids);
    }
}
