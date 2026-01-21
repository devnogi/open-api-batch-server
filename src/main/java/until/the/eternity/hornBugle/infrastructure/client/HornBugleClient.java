package until.the.eternity.hornBugle.infrastructure.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryListResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class HornBugleClient {

    private final WebClient openApiWebClient;

    /**
     * 서버별 뿔피리 히스토리 조회.
     *
     * @param server 조회할 서버
     * @return 응답 DTO를 담은 Mono, 호출 실패 시 Mono.empty()
     */
    public Mono<OpenApiHornBugleHistoryListResponse> fetchHornBugleHistory(HornBugleServer server) {
        log.info(
                "[SCHEDULE] [HornBugle] Calling Nexon Open API Horn Bugle History API for server='{}'",
                server.getServerName());

        return openApiWebClient
                .get()
                .uri(
                        uriBuilder ->
                                uriBuilder
                                        .path("/horn-bugle-world/history")
                                        .queryParam("server_name", server.getEncodedServerName())
                                        .build())
                .retrieve()
                .bodyToMono(OpenApiHornBugleHistoryListResponse.class)
                .onErrorResume(
                        throwable -> {
                            log.warn(
                                    "[SCHEDULE] [HornBugle] Failed to fetch Nexon Open API Horn Bugle History API for server='{}': error='{}', message='{}'",
                                    server.getServerName(),
                                    throwable.toString(),
                                    throwable.getMessage());
                            return Mono.empty();
                        });
    }
}
