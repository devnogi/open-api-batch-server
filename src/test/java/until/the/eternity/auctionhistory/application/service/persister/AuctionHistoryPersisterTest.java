package until.the.eternity.auctionhistory.application.service.persister;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryPersisterTest {

    @Mock private OpenApiAuctionHistoryMapper mapper;

    @Mock private AuctionHistoryDuplicateChecker duplicateChecker;

    @InjectMocks private AuctionHistoryPersister auctionHistoryPersister;

    private List<OpenApiAuctionHistoryResponse> dtoList;
    private List<OpenApiAuctionHistoryResponse> filteredDtoList;
    private List<AuctionHistory> entities;
    private ItemCategory category;

    @BeforeEach
    void setUp() {
        category = ItemCategory.SWORD;

        OpenApiAuctionHistoryResponse dto1 = mock(OpenApiAuctionHistoryResponse.class);
        OpenApiAuctionHistoryResponse dto2 = mock(OpenApiAuctionHistoryResponse.class);
        dtoList = Arrays.asList(dto1, dto2);

        filteredDtoList = Arrays.asList(dto1);

        AuctionHistory entity1 = mock(AuctionHistory.class);
        AuctionHistory entity2 = mock(AuctionHistory.class);
        entities = Arrays.asList(entity1, entity2);
    }

    @Test
    @DisplayName("새로운 경매 기록이 있을 때 필터링된 엔티티 리스트를 반환한다")
    void filterOutExisting_WhenNewRecordsExist_ShouldReturnFilteredEntities() {
        // given
        when(duplicateChecker.filterExisting(dtoList, category)).thenReturn(filteredDtoList);
        when(mapper.toEntityList(filteredDtoList, category)).thenReturn(entities);

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(dtoList, category);

        // then
        assertThat(actualEntities).isEqualTo(entities);
        verify(duplicateChecker).filterExisting(dtoList, category);
        verify(mapper).toEntityList(filteredDtoList, category);
    }

    @Test
    @DisplayName("새로운 경매 기록이 없을 때 빈 리스트를 반환한다")
    void filterOutExisting_WhenNoNewRecords_ShouldReturnEmptyList() {
        // given
        when(duplicateChecker.filterExisting(dtoList, category))
                .thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(dtoList, category);

        // then
        assertThat(actualEntities).isEmpty();
        verify(duplicateChecker).filterExisting(dtoList, category);
        verify(mapper).toEntityList(Collections.emptyList(), category);
    }

    @Test
    @DisplayName("빈 DTO 리스트가 주어졌을 때 빈 리스트를 반환한다")
    void filterOutExisting_WhenEmptyDtoList_ShouldReturnEmptyList() {
        // given
        List<OpenApiAuctionHistoryResponse> emptyList = Collections.emptyList();
        when(duplicateChecker.filterExisting(emptyList, category))
                .thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(emptyList, category);

        // then
        assertThat(actualEntities).isEmpty();
        verify(duplicateChecker).filterExisting(emptyList, category);
        verify(mapper).toEntityList(Collections.emptyList(), category);
    }

    @Test
    @DisplayName("DTO 리스트에 null이 포함되어 있어도 정상적으로 처리한다")
    void filterOutExisting_WhenDtoListContainsNull_ShouldProcessCorrectly() {
        // given
        OpenApiAuctionHistoryResponse dto1 = mock(OpenApiAuctionHistoryResponse.class);
        List<OpenApiAuctionHistoryResponse> listWithNulls = Arrays.asList(dto1, null);
        List<OpenApiAuctionHistoryResponse> filteredList = List.of(dto1);

        when(duplicateChecker.filterExisting(listWithNulls, category)).thenReturn(filteredList);
        when(mapper.toEntityList(filteredList, category)).thenReturn(entities);

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(listWithNulls, category);

        // then
        assertThat(actualEntities).isEqualTo(entities);
        verify(duplicateChecker).filterExisting(listWithNulls, category);
        verify(mapper).toEntityList(filteredList, category);
    }

    @Test
    @DisplayName("일부 DTO만 중복일 경우, 중복되지 않은 DTO만 변환한다")
    void filterOutExisting_WhenSomeDtosAreDuplicate_ShouldConvertNonDuplicates() {
        // given
        OpenApiAuctionHistoryResponse dto1 = mock(OpenApiAuctionHistoryResponse.class);
        OpenApiAuctionHistoryResponse dto2 = mock(OpenApiAuctionHistoryResponse.class);
        OpenApiAuctionHistoryResponse dto3 = mock(OpenApiAuctionHistoryResponse.class);
        List<OpenApiAuctionHistoryResponse> originalList = Arrays.asList(dto1, dto2, dto3);

        List<OpenApiAuctionHistoryResponse> nonDuplicateList = Arrays.asList(dto1, dto3);
        List<AuctionHistory> expectedEntities = List.of(mock(AuctionHistory.class));

        when(duplicateChecker.filterExisting(originalList, category)).thenReturn(nonDuplicateList);
        when(mapper.toEntityList(nonDuplicateList, category)).thenReturn(expectedEntities);

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(originalList, category);

        // then
        assertThat(actualEntities).isEqualTo(expectedEntities);
        verify(duplicateChecker).filterExisting(originalList, category);
        verify(mapper).toEntityList(nonDuplicateList, category);
    }
}
