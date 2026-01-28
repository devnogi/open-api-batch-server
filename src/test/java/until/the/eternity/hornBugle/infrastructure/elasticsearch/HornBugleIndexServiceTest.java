package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HornBugleIndexServiceTest {

    @Mock private ElasticsearchOperations elasticsearchOperations;
    @Mock private HornBugleElasticsearchRepository repository;

    @InjectMocks private HornBugleIndexService indexService;

    @Nested
    @DisplayName("index 메서드 테스트")
    class IndexTest {

        @Test
        @DisplayName("단일 엔티티를 색인한다")
        void index_shouldSaveDocument() {
            // given
            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");

            // when
            indexService.index(entity);

            // then
            verify(repository).save(any(HornBugleDocument.class));
        }

        @Test
        @DisplayName("색인 실패 시 예외를 던지지 않고 로그만 기록한다")
        void index_onFailure_shouldNotThrowException() {
            // given
            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");
            when(repository.save(any(HornBugleDocument.class)))
                    .thenThrow(new RuntimeException("ES error"));

            // when & then (no exception)
            indexService.index(entity);
            verify(repository).save(any(HornBugleDocument.class));
        }
    }

    @Nested
    @DisplayName("indexAll 메서드 테스트")
    class IndexAllTest {

        @Test
        @DisplayName("여러 엔티티를 일괄 색인한다")
        void indexAll_shouldSaveAllDocuments() {
            // given
            List<HornBugleWorldHistory> entities =
                    List.of(
                            createEntity(1L, "류트", "테스트1", "안녕하세요1"),
                            createEntity(2L, "만돌린", "테스트2", "안녕하세요2"));

            // when
            indexService.indexAll(entities);

            // then
            verify(repository).saveAll(anyList());
        }

        @Test
        @DisplayName("빈 리스트면 저장하지 않는다")
        void indexAll_withEmptyList_shouldNotSave() {
            // when
            indexService.indexAll(List.of());

            // then
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("null이면 저장하지 않는다")
        void indexAll_withNull_shouldNotSave() {
            // when
            indexService.indexAll(null);

            // then
            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("recreateIndex 메서드 테스트")
    class RecreateIndexTest {

        @Test
        @DisplayName("기존 인덱스가 있으면 삭제 후 재생성한다")
        void recreateIndex_withExistingIndex_shouldDeleteAndCreate() {
            // given
            IndexOperations indexOps = mock(IndexOperations.class);
            when(elasticsearchOperations.indexOps(HornBugleDocument.class)).thenReturn(indexOps);
            when(indexOps.exists()).thenReturn(true);
            when(indexOps.createWithMapping()).thenReturn(true);

            // when
            indexService.recreateIndex();

            // then
            verify(indexOps).delete();
            verify(indexOps).createWithMapping();
        }

        @Test
        @DisplayName("기존 인덱스가 없으면 생성만 한다")
        void recreateIndex_withoutExistingIndex_shouldOnlyCreate() {
            // given
            IndexOperations indexOps = mock(IndexOperations.class);
            when(elasticsearchOperations.indexOps(HornBugleDocument.class)).thenReturn(indexOps);
            when(indexOps.exists()).thenReturn(false);
            when(indexOps.createWithMapping()).thenReturn(true);

            // when
            indexService.recreateIndex();

            // then
            verify(indexOps, never()).delete();
            verify(indexOps).createWithMapping();
        }

        @Test
        @DisplayName("인덱스 생성 실패 시 예외를 던진다")
        void recreateIndex_onFailure_shouldThrowException() {
            // given
            IndexOperations indexOps = mock(IndexOperations.class);
            when(elasticsearchOperations.indexOps(HornBugleDocument.class)).thenReturn(indexOps);
            when(indexOps.exists()).thenThrow(new RuntimeException("ES error"));

            // when & then
            assertThatThrownBy(() -> indexService.recreateIndex())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to recreate Elasticsearch index");
        }
    }

    @Nested
    @DisplayName("search 메서드 테스트")
    class SearchTest {

        @Test
        @DisplayName("검색 결과를 반환한다")
        @SuppressWarnings("unchecked")
        void search_shouldReturnResults() {
            // given
            String keyword = "테스트";
            String serverName = "류트";
            Pageable pageable = PageRequest.of(0, 20);

            HornBugleDocument document =
                    HornBugleDocument.builder()
                            .id("1")
                            .serverName("류트")
                            .characterName("테스트")
                            .message("안녕하세요")
                            .dateSend(Instant.now())
                            .dateRegister(Instant.now())
                            .build();

            SearchHit<HornBugleDocument> searchHit = mock(SearchHit.class);
            when(searchHit.getContent()).thenReturn(document);

            SearchHits<HornBugleDocument> searchHits = mock(SearchHits.class);
            when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
            when(searchHits.getTotalHits()).thenReturn(1L);

            when(elasticsearchOperations.search(
                            any(NativeQuery.class),
                            eq(HornBugleDocument.class),
                            any(IndexCoordinates.class)))
                    .thenReturn(searchHits);

            // when
            Page<HornBugleDocument> result = indexService.search(keyword, serverName, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCharacterName()).isEqualTo("테스트");
            assertThat(result.getTotalElements()).isEqualTo(1L);
        }

        @Test
        @DisplayName("검색 실패 시 예외를 던진다")
        void search_onFailure_shouldThrowException() {
            // given
            String keyword = "테스트";
            String serverName = "류트";
            Pageable pageable = PageRequest.of(0, 20);

            when(elasticsearchOperations.search(
                            any(NativeQuery.class),
                            eq(HornBugleDocument.class),
                            any(IndexCoordinates.class)))
                    .thenThrow(new RuntimeException("ES error"));

            // when & then
            assertThatThrownBy(() -> indexService.search(keyword, serverName, pageable))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Elasticsearch search failed");
        }
    }

    private HornBugleWorldHistory createEntity(
            Long id, String serverName, String characterName, String message) {
        return HornBugleWorldHistory.builder()
                .id(id)
                .serverName(serverName)
                .characterName(characterName)
                .message(message)
                .dateSend(Instant.now())
                .dateRegister(Instant.now())
                .build();
    }
}
