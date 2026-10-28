package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/** Elasticsearch 설정 클래스. elasticsearch.enabled=true일 때만 관련 구성을 활성화한다. */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class HornBugleElasticsearchConfig {}
