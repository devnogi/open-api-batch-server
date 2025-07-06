package until.the.eternity.config.openapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** 외부 API용 WebClient 설정값 홀더 application.yml 사용) */
@Validated
@ConfigurationProperties(prefix = "openapi.nexon")
public record OpenApiWebClientProperties(
        @NotBlank String baseUrl,
        @NotBlank String apiKey,
        @Positive int maxInMemorySizeMb,
        @Positive int defaultTimeoutSeconds) {
    public Duration defaultTimeout() {
        return Duration.ofSeconds(defaultTimeoutSeconds);
    }
}
