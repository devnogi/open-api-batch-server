package until.the.eternity.hornBugle.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.domain.mapper.HornBugleMapper;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;
import until.the.eternity.hornBugle.domain.service.HornBugleDuplicateChecker;
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleDocument;
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleIndexService;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

@ExtendWith(MockitoExtension.class)
class HornBugleServiceTest {

    @Mock private HornBugleRepositoryPort repository;
    @Mock private HornBugleDuplicateChecker duplicateChecker;
    @Mock private HornBugleMapper mapper;
    @Mock private HornBugleIndexService indexService;

    @Nested
    @DisplayName("Elasticsearch 활성화 상태에서 search 메서드 테스트")
    class SearchWithElasticsearchTest {

        private HornBugleService service;

        @BeforeEach
        void setUp() {
            service =
                    new HornBugleService(
                            repository, duplicateChecker, mapper, Optional.of(indexService));
        }

        @Test
        @DisplayName("keyword가 null이면 DB 검색을 수행한다")
        void search_withNullKeyword_shouldSearchDatabase() {
            // given
            String serverName = "류트";
            String keyword = null;
            HornBuglePageRequestDto pageRequest = new HornBuglePageRequestDto(1, 20);
            Pageable pageable = pageRequest.toPageable();

            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");
            HornBugleHistoryResponse response = createResponse(1L, "류트", "테스트", "안녕하세요");
            Page<HornBugleWorldHistory> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findByServerName(serverName, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            // when
            PageResponseDto<HornBugleHistoryResponse> result =
                    service.search(serverName, keyword, pageRequest);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).characterName()).isEqualTo("테스트");
            verify(repository).findByServerName(serverName, pageable);
            verifyNoInteractions(indexService);
        }

        @Test
        @DisplayName("keyword가 빈 문자열이면 DB 검색을 수행한다")
        void search_withEmptyKeyword_shouldSearchDatabase() {
            // given
            String serverName = null;
            String keyword = "";
            HornBuglePageRequestDto pageRequest = new HornBuglePageRequestDto(1, 20);
            Pageable pageable = pageRequest.toPageable();

            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");
            HornBugleHistoryResponse response = createResponse(1L, "류트", "테스트", "안녕하세요");
            Page<HornBugleWorldHistory> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            // when
            PageResponseDto<HornBugleHistoryResponse> result =
                    service.search(serverName, keyword, pageRequest);

            // then
            assertThat(result.items()).hasSize(1);
            verify(repository).findAll(pageable);
            verifyNoInteractions(indexService);
        }

        @Test
        @DisplayName("keyword가 있으면 Elasticsearch 검색을 수행한다")
        void search_withKeyword_shouldSearchElasticsearch() {
            // given
            String serverName = "류트";
            String keyword = "테스트";
            HornBuglePageRequestDto pageRequest = new HornBuglePageRequestDto(1, 20);
            Pageable pageable = pageRequest.toPageable();

            HornBugleDocument document = createDocument("1", "류트", "테스트", "안녕하세요");
            HornBugleHistoryResponse response = createResponse(1L, "류트", "테스트", "안녕하세요");
            Page<HornBugleDocument> documentPage = new PageImpl<>(List.of(document), pageable, 1);

            when(indexService.search(keyword, serverName, pageable)).thenReturn(documentPage);
            when(mapper.toResponse(document)).thenReturn(response);

            // when
            PageResponseDto<HornBugleHistoryResponse> result =
                    service.search(serverName, keyword, pageRequest);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).characterName()).isEqualTo("테스트");
            verify(indexService).search(keyword, serverName, pageable);
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("serverName이 null이고 keyword가 있으면 Elasticsearch 전체 검색을 수행한다")
        void search_withKeywordAndNoServerName_shouldSearchElasticsearchWithoutFilter() {
            // given
            String serverName = null;
            String keyword = "테스트";
            HornBuglePageRequestDto pageRequest = new HornBuglePageRequestDto(1, 20);
            Pageable pageable = pageRequest.toPageable();

            HornBugleDocument document = createDocument("1", "류트", "테스트", "안녕하세요");
            HornBugleHistoryResponse response = createResponse(1L, "류트", "테스트", "안녕하세요");
            Page<HornBugleDocument> documentPage = new PageImpl<>(List.of(document), pageable, 1);

            when(indexService.search(keyword, serverName, pageable)).thenReturn(documentPage);
            when(mapper.toResponse(document)).thenReturn(response);

            // when
            PageResponseDto<HornBugleHistoryResponse> result =
                    service.search(serverName, keyword, pageRequest);

            // then
            assertThat(result.items()).hasSize(1);
            verify(indexService).search(keyword, null, pageable);
        }
    }

    @Nested
    @DisplayName("Elasticsearch 비활성화 상태에서 search 메서드 테스트")
    class SearchWithoutElasticsearchTest {

        private HornBugleService service;

        @BeforeEach
        void setUp() {
            service = new HornBugleService(repository, duplicateChecker, mapper, Optional.empty());
        }

        @Test
        @DisplayName("keyword가 있어도 ES가 비활성화되어 있으면 DB 검색을 수행한다")
        void search_withKeywordButNoEs_shouldFallbackToDatabase() {
            // given
            String serverName = "류트";
            String keyword = "테스트";
            HornBuglePageRequestDto pageRequest = new HornBuglePageRequestDto(1, 20);
            Pageable pageable = pageRequest.toPageable();

            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");
            HornBugleHistoryResponse response = createResponse(1L, "류트", "테스트", "안녕하세요");
            Page<HornBugleWorldHistory> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findByServerName(serverName, pageable)).thenReturn(entityPage);
            when(mapper.toResponse(entity)).thenReturn(response);

            // when
            PageResponseDto<HornBugleHistoryResponse> result =
                    service.search(serverName, keyword, pageRequest);

            // then
            assertThat(result.items()).hasSize(1);
            verify(repository).findByServerName(serverName, pageable);
        }
    }

    @Nested
    @DisplayName("saveAll 메서드 테스트")
    class SaveAllTest {

        private HornBugleService service;

        @BeforeEach
        void setUp() {
            service =
                    new HornBugleService(
                            repository, duplicateChecker, mapper, Optional.of(indexService));
        }

        @Test
        @DisplayName("새 데이터 저장 시 Elasticsearch에도 색인한다")
        void saveAll_shouldIndexToElasticsearch() {
            // given
            HornBugleServer server = HornBugleServer.LUTE;
            OpenApiHornBugleHistoryResponse apiResponse =
                    new OpenApiHornBugleHistoryResponse("테스트", "안녕하세요", Instant.now());
            List<OpenApiHornBugleHistoryResponse> responses = List.of(apiResponse);

            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");

            when(duplicateChecker.filterDuplicates(server, responses)).thenReturn(responses);
            when(mapper.toEntity(eq(apiResponse), eq(server), any(Instant.class)))
                    .thenReturn(entity);

            // when
            int savedCount = service.saveAll(server, responses);

            // then
            assertThat(savedCount).isEqualTo(1);
            verify(repository).saveAll(anyList());
            verify(indexService).indexAll(anyList());
        }

        @Test
        @DisplayName("모든 데이터가 중복이면 저장하지 않는다")
        void saveAll_withAllDuplicates_shouldNotSave() {
            // given
            HornBugleServer server = HornBugleServer.LUTE;
            OpenApiHornBugleHistoryResponse apiResponse =
                    new OpenApiHornBugleHistoryResponse("테스트", "안녕하세요", Instant.now());
            List<OpenApiHornBugleHistoryResponse> responses = List.of(apiResponse);

            when(duplicateChecker.filterDuplicates(server, responses)).thenReturn(List.of());

            // when
            int savedCount = service.saveAll(server, responses);

            // then
            assertThat(savedCount).isEqualTo(0);
            verifyNoInteractions(repository);
            verifyNoInteractions(indexService);
        }

        @Test
        @DisplayName("빈 응답이면 저장하지 않는다")
        void saveAll_withEmptyResponses_shouldNotSave() {
            // given
            HornBugleServer server = HornBugleServer.LUTE;
            List<OpenApiHornBugleHistoryResponse> responses = List.of();

            // when
            int savedCount = service.saveAll(server, responses);

            // then
            assertThat(savedCount).isEqualTo(0);
            verifyNoInteractions(duplicateChecker);
            verifyNoInteractions(repository);
            verifyNoInteractions(indexService);
        }
    }

    @Nested
    @DisplayName("ES 비활성화 상태에서 saveAll 메서드 테스트")
    class SaveAllWithoutElasticsearchTest {

        private HornBugleService service;

        @BeforeEach
        void setUp() {
            service = new HornBugleService(repository, duplicateChecker, mapper, Optional.empty());
        }

        @Test
        @DisplayName("ES가 비활성화되어 있으면 DB에만 저장한다")
        void saveAll_withoutEs_shouldOnlySaveToDatabase() {
            // given
            HornBugleServer server = HornBugleServer.LUTE;
            OpenApiHornBugleHistoryResponse apiResponse =
                    new OpenApiHornBugleHistoryResponse("테스트", "안녕하세요", Instant.now());
            List<OpenApiHornBugleHistoryResponse> responses = List.of(apiResponse);

            HornBugleWorldHistory entity = createEntity(1L, "류트", "테스트", "안녕하세요");

            when(duplicateChecker.filterDuplicates(server, responses)).thenReturn(responses);
            when(mapper.toEntity(eq(apiResponse), eq(server), any(Instant.class)))
                    .thenReturn(entity);

            // when
            int savedCount = service.saveAll(server, responses);

            // then
            assertThat(savedCount).isEqualTo(1);
            verify(repository).saveAll(anyList());
            // indexService는 Optional.empty()이므로 호출되지 않음
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

    private HornBugleDocument createDocument(
            String id, String serverName, String characterName, String message) {
        return HornBugleDocument.builder()
                .id(id)
                .serverName(serverName)
                .characterName(characterName)
                .message(message)
                .dateSend(Instant.now())
                .dateRegister(Instant.now())
                .build();
    }

    private HornBugleHistoryResponse createResponse(
            Long id, String serverName, String characterName, String message) {
        return new HornBugleHistoryResponse(
                id, serverName, characterName, message, Instant.now(), Instant.now());
    }
}
