package until.the.eternity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

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
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-nxopen-api-key", apiKey)
                .build();
    }
}
