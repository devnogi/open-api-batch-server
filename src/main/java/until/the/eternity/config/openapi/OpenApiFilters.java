package until.the.eternity.config.openapi;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenApiFilters {

    private final OpenApiRetryPolicy retryPolicy;

    /** 5xx 응답에 대해 재시도 정책 적용 */
    public ExchangeFilterFunction retryFilter() {
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

    /** 호출 타임아웃 필터 */
    public ExchangeFilterFunction timeoutFilter(Duration timeout) {
        return (request, next) -> next.exchange(request).timeout(timeout);
    }

    /** 오류 로깅 필터 */
    public ExchangeFilterFunction errorLoggingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(
                response -> {
                    if (response.statusCode().isError()) {
                        log.warn("WebClient error: status={}", response.statusCode());
                    }
                    return Mono.just(response);
                });
    }
}
