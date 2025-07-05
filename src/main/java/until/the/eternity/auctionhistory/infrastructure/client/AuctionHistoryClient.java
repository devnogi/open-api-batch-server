package until.the.eternity.auctionhistory.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryListResponse;
import until.the.eternity.common.enums.ItemCategory;

/**
 * Nexon OPEN API 호출 전담 클라이언트.
 *
 * <p>– 전역 WebClient 설정(필터 · 헤더 · 타임아웃 · 재시도)은 {@link
 * until.the.eternity.config.openapi.OpenApiWebClientConfig} 에서 담당한다. – 이 클래스는 “엔드포인트·쿼리 파라미터·로깅” 만
 * 책임지는 SRP 구조다.
 */
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
     * @return 응답 DTO, 호출 실패 시 null
     */
    public OpenApiAuctionHistoryListResponse fetchAuctionHistory(
            ItemCategory category, String cursor) {

        try {
            return openApiWebClient
                    .get()
                    .uri(
                            uriBuilder ->
                                    uriBuilder
                                            .path("/auction/history")
                                            .queryParam(
                                                    "auction_item_category",
                                                    category.getSubCategory())
                                            .queryParamIfPresent(
                                                    "cursor",
                                                    Mono.justOrEmpty(cursor).blockOptional())
                                            .build())
                    .retrieve()
                    .bodyToMono(OpenApiAuctionHistoryListResponse.class)
                    // 필터에서 재시도·타임아웃·에러로깅이 이미 적용됨
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
}
