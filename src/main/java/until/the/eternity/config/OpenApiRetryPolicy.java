package until.the.eternity.config;

import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

/**
 * Nexon OPEN API 전용 재시도(Back-off) 정책.
 *
 * <p>변경이 잦은 정책 로직을 Config에서 분리해 SOLID 원칙을 지킨다.
 */
@Component
public class OpenApiRetryPolicy {

    private static final int MAX_RETRIES = 3;
    private static final Duration INITIAL_DELAY = Duration.ofSeconds(2);

    /** 5xx 오류에 한해 지수 백오프 재시도 스펙 반환 */
    public RetryBackoffSpec retrySpec() {
        return Retry.backoff(MAX_RETRIES, INITIAL_DELAY)
                .filter(
                        throwable ->
                                throwable instanceof WebClientResponseException
                                        && ((WebClientResponseException) throwable)
                                                .getStatusCode()
                                                .is5xxServerError())
                .onRetryExhaustedThrow((spec, signal) -> signal.failure());
    }
}
