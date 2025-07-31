package until.the.eternity.auctionhistory.infrastructure.client;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
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
            // URI 구성
            UriComponents uriComponents =
                    UriComponentsBuilder.newInstance()
                            .path("/auction/history")
                            .queryParam("auction_item_category", category.getSubCategory())
                            .queryParamIfPresent("cursor", Optional.ofNullable(cursor))
                            .build();

            log.info("Calling Nexon OPEN API URI: {}", uriComponents.toUriString());

            return openApiWebClient
                    .get()
                    .uri(uriComponents.toUri())
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse ->
                                    clientResponse
                                            .bodyToMono(String.class)
                                            .flatMap(
                                                    errorBody -> {
                                                        log.error(
                                                                "Received error response: status={}, body={}",
                                                                clientResponse.statusCode(),
                                                                errorBody);
                                                        return Mono.error(
                                                                new RuntimeException(
                                                                        "API response error: "
                                                                                + errorBody));
                                                    }))
                    .bodyToMono(OpenApiAuctionHistoryListResponse.class)
                    .onErrorResume(
                            throwable -> {
                                log.error(
                                        "Exception while calling auction history API [category={}, cursor={}]",
                                        category,
                                        cursor,
                                        throwable);
                                return Mono.empty(); // graceful fail
                            })
                    .block();

        } catch (Exception ex) {
            log.error("Unexpected exception during auction history fetch", ex);
            return null;
        }
    }
}
