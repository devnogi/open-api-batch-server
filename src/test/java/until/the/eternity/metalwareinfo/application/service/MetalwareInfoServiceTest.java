package until.the.eternity.metalwareinfo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareInfoRepositoryPort;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoResponse;

@ExtendWith(MockitoExtension.class)
class MetalwareInfoServiceTest {

    @Mock private MetalwareInfoRepositoryPort repositoryPort;

    @InjectMocks private MetalwareInfoService service;

    @Test
    @DisplayName("모든 세공 정보를 조회하면 세공 목록을 반환한다")
    void findAll_should_return_all_metalwares() {
        // given
        List<String> metalwares = List.of("불의 연성술", "물의 연성술", "바람의 연성술");
        when(repositoryPort.findAllMetalwares()).thenReturn(metalwares);

        // when
        List<MetalwareInfoResponse> result = service.findAll();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).metalware()).isEqualTo("불의 연성술");
        assertThat(result.get(1).metalware()).isEqualTo("물의 연성술");
        assertThat(result.get(2).metalware()).isEqualTo("바람의 연성술");
        verify(repositoryPort).findAllMetalwares();
    }

    @Test
    @DisplayName("세공 정보가 없으면 빈 목록을 반환한다")
    void findAll_should_return_empty_list_when_no_data() {
        // given
        when(repositoryPort.findAllMetalwares()).thenReturn(List.of());

        // when
        List<MetalwareInfoResponse> result = service.findAll();

        // then
        assertThat(result).isEmpty();
        verify(repositoryPort).findAllMetalwares();
    }
}
