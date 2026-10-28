package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface HornBugleElasticsearchRepository
        extends ElasticsearchRepository<HornBugleDocument, String> {}
