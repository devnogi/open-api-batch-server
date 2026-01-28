package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/** Elasticsearch 설정 클래스. elasticsearch.enabled=true일 때만 Elasticsearch Repository를 활성화한다. */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(
        basePackages = "until.the.eternity.hornBugle.infrastructure.elasticsearch")
public class HornBugleElasticsearchConfig {}
