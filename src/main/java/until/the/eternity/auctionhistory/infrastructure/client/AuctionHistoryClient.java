package until.the.eternity.auctionhistory.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryListResponse;
import until.the.eternity.common.enums.ItemCategory;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionHistoryClient {

    /** `OpenApiWebClientConfig.openApiWebClient()` 로 만든 빈이 주입된다. */
    private final WebClient openApiWebClient;

    /**
     * 카테고리·커서 기반 경매 히스토리 조회.
     *
     * @param category 조회할 카테고리
     * @param cursor 다음 페이지 커서(null 가능)
     * @return 응답 DTO를 담은 Mono, 호출 실패 시 Mono.empty()
     */
    public Mono<OpenApiAuctionHistoryListResponse> fetchAuctionHistory(
            ItemCategory category, String cursor) {

        log.info(
                "[SCHEDULE] [{}] Calling Nexon Open API Auction History API with cursor='{}'",
                category.getSubCategory(),
                cursor == null ? "" : cursor);

        return openApiWebClient
                .get()
                .uri(
                        uriBuilder -> {
                            uriBuilder
                                    .path("/auction/history")
                                    .queryParam("auction_item_category", category.getSubCategory());
                            if (cursor != null) {
                                uriBuilder.queryParam("cursor", cursor);
                            }
                            return uriBuilder.build();
                        })
                .retrieve()
                .bodyToMono(OpenApiAuctionHistoryListResponse.class)
                // 필터에서 재시도·타임아웃·에러로깅이 이미 적용됨
                .onErrorResume(
                        throwable -> {
                            log.warn(
                                    "[SCHEDULE] [{}] Failed to fetch Nexon Open API Auction History API with cursor='{}': error='{}', message='{}'",
                                    category.getSubCategory(),
                                    cursor,
                                    throwable.toString(),
                                    throwable.getMessage());
                            return Mono.empty(); // graceful fail
                        });
    }
}