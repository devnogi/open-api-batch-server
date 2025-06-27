package until.the.eternity.config;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class OpenAPIClientConfig {

    private final String apiKey;
    private final OpenApiRetryPolicy retryPolicy;

    public OpenAPIClientConfig(
            @Value("${openapi.nexon.api-key}") String apiKey, OpenApiRetryPolicy retryPolicy) {
        this.apiKey = apiKey;
        this.retryPolicy = retryPolicy;
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs().maxInMemorySize(5 * 1024 * 1024))
                                .build())
                .baseUrl("https://open.api.nexon.com/mabinogi/v1")
                .filter(retryFilter()) // 🔁 재시도 필터
                .filter(timeoutFilter()) // ⏰ 타임아웃 필터
                .filter(errorLoggingFilter()) // ⚠️ 예외 로깅 필터
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-nxopen-api-key", apiKey)
                .build();
    }

    /** 재시도 필터 – 5xx 응답 시 정책에 따라 retry. */
    private ExchangeFilterFunction retryFilter() {
        return (request, next) ->
                next.exchange(request)
                        .flatMap(
                                response -> {
                                    if (response.statusCode().is5xxServerError()) {
                                        return Mono.error(new RuntimeException());
                                    }
                                    return Mono.just(response);
                                })
                        .retryWhen(retryPolicy.retrySpec());
    }

    /** 호출 타임아웃(5초) 필터 */
    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> next.exchange(request).timeout(Duration.ofSeconds(5));
    }

    /** 오류 로깅 필터 */
    private ExchangeFilterFunction errorLoggingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(
                clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        log.warn("WebClient error: status={}", clientResponse.statusCode());
                    }
                    return Mono.just(clientResponse);
                });
    }
}
