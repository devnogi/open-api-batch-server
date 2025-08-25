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
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(filteredDtoList);
        when(mapper.toEntityList(filteredDtoList, category)).thenReturn(entities);

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(dtoList, category);

        // then
        assertThat(actualEntities).isEqualTo(entities);
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(filteredDtoList, category);
    }

    @Test
    @DisplayName("새로운 경매 기록이 없을 때 빈 리스트를 반환한다")
    void filterOutExisting_WhenNoNewRecords_ShouldReturnEmptyList() {
        // given
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(dtoList, category);

        // then
        assertThat(actualEntities).isEmpty();
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(Collections.emptyList(), category);
    }

    @Test
    @DisplayName("빈 DTO 리스트가 주어졌을 때 빈 리스트를 반환한다")
    void filterOutExisting_WhenEmptyDtoList_ShouldReturnEmptyList() {
        // given
        List<OpenApiAuctionHistoryResponse> emptyList = Collections.emptyList();
        when(duplicateChecker.filterExisting(emptyList)).thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        List<AuctionHistory> actualEntities =
                auctionHistoryPersister.filterOutExisting(emptyList, category);

        // then
        assertThat(actualEntities).isEmpty();
        verify(duplicateChecker).filterExisting(emptyList);
        verify(mapper).toEntityList(Collections.emptyList(), category);
    }
}
