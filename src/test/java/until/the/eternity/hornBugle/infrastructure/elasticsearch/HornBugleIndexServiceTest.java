package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

@ExtendWith(MockitoExtension.class)
class HornBugleIndexServiceTest {

    @Mock private ElasticsearchOperations elasticsearchOperations;
    @Mock private SearchHits<HornBugleDocument> searchHits;

    private HornBugleIndexService service;

    @BeforeEach
    void setUp() {
        service = new HornBugleIndexService(elasticsearchOperations);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);
        when(elasticsearchOperations.search(
                        any(NativeQuery.class),
                        eq(HornBugleDocument.class),
                        any(IndexCoordinates.class)))
                .thenReturn(searchHits);
    }

    @Test
    void keywordSearchIncludesCharacterMessageAndExactServerName() {
        service.search("류트", null, PageRequest.of(0, 20));

        NativeQuery query = capturedQuery();
        BoolQuery bool = query.getQuery().bool();

        assertThat(bool.minimumShouldMatch()).isEqualTo("1");
        assertThat(bool.should()).hasSize(2);
        assertThat(bool.should().stream().filter(Query::isMultiMatch).findFirst())
                .hasValueSatisfying(
                        textQuery ->
                                assertThat(textQuery.multiMatch().fields())
                                        .containsExactly("character_name", "message"));
        assertThat(bool.should().stream().filter(Query::isTerm).findFirst())
                .hasValueSatisfying(
                        serverQuery -> {
                            assertThat(serverQuery.term().field()).isEqualTo("server_name");
                            assertThat(serverQuery.term().value().stringValue()).isEqualTo("류트");
                        });
        assertThat(bool.filter()).isEmpty();
    }

    @Test
    void explicitServerFilterRemainsInAdditionToUnifiedKeywordSearch() {
        service.search("모집", "하프", PageRequest.of(0, 20));

        BoolQuery bool = capturedQuery().getQuery().bool();

        assertThat(bool.should()).hasSize(2);
        assertThat(bool.filter())
                .singleElement()
                .satisfies(
                        serverFilter -> {
                            assertThat(serverFilter.term().field()).isEqualTo("server_name");
                            assertThat(serverFilter.term().value().stringValue()).isEqualTo("하프");
                        });
    }

    private NativeQuery capturedQuery() {
        ArgumentCaptor<NativeQuery> captor = ArgumentCaptor.forClass(NativeQuery.class);
        verify(elasticsearchOperations)
                .search(captor.capture(), eq(HornBugleDocument.class), any(IndexCoordinates.class));
        return captor.getValue();
    }
}
