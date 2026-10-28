package until.the.eternity.auctionrealtime.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeListResponse;
import until.the.eternity.common.enums.ItemCategory;

/** Nexon Open API /auction/list 엔드포인트 호출 클라이언트. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionRealtimeClient {

    private final WebClient openApiWebClient;

    /**
     * 카테고리·커서 기반 경매장 현재 판매 목록 조회.
     *
     * @param category 조회할 카테고리
     * @param cursor 다음 페이지 커서(null 가능)
     * @return 응답 DTO를 담은 Mono, 호출 실패 시 Mono.empty()
     */
    public Mono<OpenApiAuctionRealtimeListResponse> fetchAuctionList(
            ItemCategory category, String cursor) {

        log.info(
                "[REALTIME] [{}] Calling Nexon Open API Auction List API with cursor='{}'",
                category.getSubCategory(),
                cursor == null ? "" : cursor);

        return openApiWebClient
                .get()
                .uri(
                        uriBuilder -> {
                            uriBuilder
                                    .path("/auction/list")
                                    .queryParam("auction_item_category", category.getSubCategory());
                            if (cursor != null && !cursor.isEmpty()) {
                                uriBuilder.queryParam("cursor", cursor);
                            }
                            return uriBuilder.build();
                        })
                .retrieve()
                .bodyToMono(OpenApiAuctionRealtimeListResponse.class)
                .onErrorResume(
                        throwable -> {
                            log.warn(
                                    "[REALTIME] [{}] Failed to fetch Nexon Open API Auction List API with cursor='{}': error='{}', message='{}'",
                                    category.getSubCategory(),
                                    cursor,
                                    throwable.toString(),
                                    throwable.getMessage());
                            return Mono.empty();
                        });
    }
}
