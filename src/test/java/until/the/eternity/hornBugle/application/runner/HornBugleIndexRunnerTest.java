package until.the.eternity.hornBugle.application.runner;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleIndexService;

@ExtendWith(MockitoExtension.class)
class HornBugleIndexRunnerTest {

    @Mock private HornBugleRepositoryPort repository;
    @Mock private HornBugleIndexService indexService;

    @InjectMocks private HornBugleIndexRunner runner;

    @Test
    @DisplayName("서버 재기동 시 인덱스를 재생성하고 DB 데이터를 일괄 색인한다")
    void run_shouldRecreateIndexAndIndexAllData() {
        // given
        ApplicationArguments args = mock(ApplicationArguments.class);

        HornBugleWorldHistory entity1 = createEntity(1L, "류트", "테스트1", "안녕하세요1");
        HornBugleWorldHistory entity2 = createEntity(2L, "만돌린", "테스트2", "안녕하세요2");

        // 첫 페이지: 데이터 있고 다음 페이지 없음 (hasNext = false)
        Page<HornBugleWorldHistory> firstPage =
                new PageImpl<>(List.of(entity1, entity2), PageRequest.of(0, 500), 2);

        when(repository.findAll(PageRequest.of(0, 500))).thenReturn(firstPage);

        // when
        runner.run(args);

        // then
        verify(indexService).recreateIndex();
        verify(indexService).indexAll(List.of(entity1, entity2));
    }

    @Test
    @DisplayName("DB에 데이터가 없으면 인덱스만 재생성한다")
    void run_withNoData_shouldOnlyRecreateIndex() {
        // given
        ApplicationArguments args = mock(ApplicationArguments.class);

        Page<HornBugleWorldHistory> emptyPage =
                new PageImpl<>(List.of(), PageRequest.of(0, 500), 0);

        when(repository.findAll(PageRequest.of(0, 500))).thenReturn(emptyPage);

        // when
        runner.run(args);

        // then
        verify(indexService).recreateIndex();
        verify(indexService, never()).indexAll(anyList());
    }

    @Test
    @DisplayName("인덱스 재생성 실패 시 예외를 로깅하고 계속 진행하지 않는다")
    void run_onRecreateIndexFailure_shouldLogAndStop() {
        // given
        ApplicationArguments args = mock(ApplicationArguments.class);
        doThrow(new RuntimeException("ES error")).when(indexService).recreateIndex();

        // when
        runner.run(args);

        // then
        verify(indexService).recreateIndex();
        verifyNoInteractions(repository);
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
