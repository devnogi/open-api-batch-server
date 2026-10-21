package until.the.eternity.auctionsearchoption.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;
import until.the.eternity.auctionsearchoption.domain.repository.AuctionSearchOptionRepositoryPort;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.FieldMetadata;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;

@ExtendWith(MockitoExtension.class)
class AuctionSearchOptionServiceTest {

    @Mock private AuctionSearchOptionRepositoryPort repositoryPort;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private AuctionSearchOptionService service;

    @Test
    @DisplayName("모든 활성화된 검색 옵션을 조회하면 응답 리스트를 반환한다")
    void getAllActiveSearchOptions_should_return_response_list() throws Exception {
        // given
        AuctionSearchOptionMetadata entity1 =
                createEntity(1L, "밸런스", "{\"balance\":{\"type\":\"tinyint\"}}", 1);
        AuctionSearchOptionMetadata entity2 =
                createEntity(2L, "공격력", "{\"attack\":{\"type\":\"int\"}}", 2);
        Map<String, FieldMetadata> fieldMetadataMap =
                Map.of("balance", new FieldMetadata("tinyint", false, null));

        when(repositoryPort.findAllActive()).thenReturn(List.of(entity1, entity2));
        when(objectMapper.readValue(
                        anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(fieldMetadataMap);

        // when
        List<SearchOptionMetadataResponse> result = service.getAllActiveSearchOptions();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).searchOptionName()).isEqualTo("밸런스");
        assertThat(result.get(1).searchOptionName()).isEqualTo("공격력");
        assertThat(result.get(0).displayOrder()).isEqualTo(1);
        assertThat(result.get(1).displayOrder()).isEqualTo(2);

        verify(repositoryPort).findAllActive();
        verify(objectMapper, times(2))
                .readValue(anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class));
    }

    @Test
    @DisplayName("활성화된 검색 옵션이 없으면 빈 리스트를 반환한다")
    void getAllActiveSearchOptions_should_return_empty_list_when_no_active_options() {
        // given
        when(repositoryPort.findAllActive()).thenReturn(List.of());

        // when
        List<SearchOptionMetadataResponse> result = service.getAllActiveSearchOptions();

        // then
        assertThat(result).isEmpty();
        verify(repositoryPort).findAllActive();
        verifyNoInteractions(objectMapper);
    }

    @Test
    @DisplayName("검색 조건 JSON을 올바르게 파싱하여 응답에 포함한다")
    void getAllActiveSearchOptions_should_parse_json_correctly() throws Exception {
        // given
        AuctionSearchOptionMetadata entity1 =
                createEntity(1L, "밸런스", "{\"balance\":{\"type\":\"tinyint\"}}", 1);
        FieldMetadata balanceField = new FieldMetadata("tinyint", false, null);
        Map<String, FieldMetadata> parsedMap = Map.of("balance", balanceField);

        when(repositoryPort.findAllActive()).thenReturn(List.of(entity1));
        when(objectMapper.readValue(
                        eq(entity1.getSearchConditionJson()),
                        any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(parsedMap);

        // when
        List<SearchOptionMetadataResponse> result = service.getAllActiveSearchOptions();

        // then
        assertThat(result).hasSize(1);
        SearchOptionMetadataResponse response = result.get(0);
        assertThat(response.searchCondition()).containsKey("balance");
        assertThat(response.searchCondition().get("balance")).isEqualTo(balanceField);

        verify(objectMapper)
                .readValue(
                        eq(entity1.getSearchConditionJson()),
                        any(com.fasterxml.jackson.core.type.TypeReference.class));
    }

    @Test
    @DisplayName("응답은 display_order 순서대로 정렬되어 있다")
    void getAllActiveSearchOptions_should_maintain_display_order() throws Exception {
        // given
        AuctionSearchOptionMetadata entity1 =
                createEntity(1L, "밸런스", "{\"balance\":{\"type\":\"tinyint\"}}", 1);
        AuctionSearchOptionMetadata entity2 =
                createEntity(2L, "공격력", "{\"attack\":{\"type\":\"int\"}}", 2);
        AuctionSearchOptionMetadata entity3 =
                createEntity(3L, "크리티컬", "{\"critical\":{\"type\":\"int\"}}", 3);
        Map<String, FieldMetadata> fieldMetadataMap =
                Map.of("balance", new FieldMetadata("tinyint", false, null));

        when(repositoryPort.findAllActive()).thenReturn(List.of(entity1, entity2, entity3));
        when(objectMapper.readValue(
                        anyString(), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(fieldMetadataMap);

        // when
        List<SearchOptionMetadataResponse> result = service.getAllActiveSearchOptions();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).displayOrder()).isEqualTo(1);
        assertThat(result.get(1).displayOrder()).isEqualTo(2);
        assertThat(result.get(2).displayOrder()).isEqualTo(3);
        assertThat(result).extracting("searchOptionName").containsExactly("밸런스", "공격력", "크리티컬");
    }

    private AuctionSearchOptionMetadata createEntity(
            Long id, String searchOptionName, String searchConditionJson, Integer displayOrder) {
        AuctionSearchOptionMetadata entity = mock(AuctionSearchOptionMetadata.class);

        when(entity.getId()).thenReturn(id);
        when(entity.getSearchOptionName()).thenReturn(searchOptionName);
        when(entity.getSearchConditionJson()).thenReturn(searchConditionJson);
        when(entity.getDisplayOrder()).thenReturn(displayOrder);

        return entity;
    }
}
