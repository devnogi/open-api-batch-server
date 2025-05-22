package until.the.eternity.statistics.config;

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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Slf4j
@Configuration
public class OpenAPIClientConfig {

    @Value("${openapi.nexon.api-key}")
    private String apiKey;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(
                                        configurer ->
                                                configurer
                                                        .defaultCodecs()
                                                        .maxInMemorySize(5 * 1024 * 1024))
                                .build())
                .baseUrl("https://open.api.nexon.com/mabinogi/v1")
                .filter(retryFilter()) // 🔁 재시도 필터
                .filter(timeoutFilter()) // ⏰ 타임아웃 필터
                .filter(errorLoggingFilter()) // ⚠️ 예외 로깅 필터
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-nxopen-api-key", apiKey)
                .build();
    }

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
                        .retryWhen(getRetrySpec());
    }

    private RetryBackoffSpec getRetrySpec() {
        return Retry.backoff(3, Duration.ofSeconds(2)) // 최대 3회, 2초부터 지수 백오프
                .filter(
                        throwable ->
                                throwable instanceof WebClientResponseException
                                        && ((WebClientResponseException) throwable)
                                                .getStatusCode()
                                                .is5xxServerError())
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }

    private ExchangeFilterFunction timeoutFilter() {
        return (request, next) -> next.exchange(request).timeout(Duration.ofSeconds(5));
    }

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
