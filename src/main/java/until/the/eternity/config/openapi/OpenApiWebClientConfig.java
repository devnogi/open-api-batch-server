package until.the.eternity.config.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OpenApiWebClientProperties.class)
@RequiredArgsConstructor
public class OpenApiWebClientConfig {

    private final OpenApiWebClientProperties props;
    private final OpenApiFilters filters;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(
                                        c ->
                                                c.defaultCodecs()
                                                        .maxInMemorySize(
                                                                props.maxInMemorySizeMb()
                                                                        * 1024
                                                                        * 1024))
                                .build())
                .baseUrl(props.baseUrl())
                .filter(filters.retryFilter())
                .filter(filters.timeoutFilter(props.defaultTimeout()))
                .filter(filters.errorLoggingFilter())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-nxopen-api-key", props.apiKey())
                .build();
    }
}
