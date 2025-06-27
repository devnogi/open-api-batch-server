package until.the.eternity.auction.client;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import until.the.eternity.auction.domain.dto.external.OpenApiAuctionHistoryListResponse;
import until.the.eternity.common.enums.ItemCategory;

/**
 * Nexon OPEN API 호출 전담 클라이언트.
 *
 * <p>재시도·예외 처리 정책을 한곳에 모아 두어, 호출자(도메인 서비스)가 단순화된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryClient {

    private final WebClient webClient;

    @Value("${openapi.nexon.api-key}")
    private String nexonApiKey;

    /**
     * 카테고리·커서 기반 경매 히스토리 조회.
     *
     * @param category 조회할 카테고리
     * @param cursor 다음 페이지 커서(null 가능)
     * @return 응답 DTO, 호출 실패 시 null
     */
    public OpenApiAuctionHistoryListResponse fetchAuctionHistory(
            ItemCategory category, String cursor) {

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
}
