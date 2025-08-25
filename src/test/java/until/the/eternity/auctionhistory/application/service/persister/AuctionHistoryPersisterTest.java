package until.the.eternity.auctionhistory.application.service.persister;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.mapper.OpenApiAuctionHistoryMapper;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.domain.service.AuctionHistoryDuplicateChecker;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryPersisterTest {

    @Mock private AuctionHistoryRepositoryPort repository;

    @Mock private OpenApiAuctionHistoryMapper mapper;

    @Mock private AuctionHistoryDuplicateChecker duplicateChecker;

    @InjectMocks private AuctionHistoryPersister auctionHistoryPersister;

    private List<OpenApiAuctionHistoryResponse> dtoList;
    private List<OpenApiAuctionHistoryResponse> filteredDtoList;
    private List<AuctionHistory> entities;
    private ItemCategory category;

    // 더미 데이터 생성 메소드
    private OpenApiAuctionHistoryResponse dummy(String id) {
        return new OpenApiAuctionHistoryResponse(
                "페러시우스 타이탄 블레이드", // itemName
                "신성한 페러시우스 타이탄 블레이드", // itemDisplayName
                ItemCategory.SWORD.getSubCategory(), // itemSubCategory
                1L, // itemCount
                100L, // auctionPricePerUnit
                Instant.now(), // dateAuctionBuy
                id, // auctionBuyId
                null // itemOption은 테스트 결과에 상관이 없으니 null 처리
                );
    }

    @BeforeEach
    void setUp() {
        category = ItemCategory.SWORD;

        // Mock DTO 데이터
        OpenApiAuctionHistoryResponse dto1 = dummy("1");
        OpenApiAuctionHistoryResponse dto2 = dummy("2");
        dtoList = Arrays.asList(dto1, dto2);

        // 중복 체크 후 필터링된 데이터
        filteredDtoList = Arrays.asList(dto1);

        // Mock Entity 데이터
        AuctionHistory entity1 = createMockAuctionHistory("item1", 2);
        AuctionHistory entity2 = createMockAuctionHistory("item2", 3);
        entities = Arrays.asList(entity1, entity2);
    }

    /** 테스트용 Mock AuctionHistory 생성 */
    private AuctionHistory createMockAuctionHistory(String itemName, int optionCount) {
        AuctionHistory entity = new AuctionHistory();
        // 실제 구현에 따라 적절한 필드 설정
        // entity.setItemName(itemName);

        if (optionCount > 0) {
            // Mock 옵션 리스트 설정 (실제 구현에 따라 조정)
            List<Object> options = Collections.nCopies(optionCount, new Object());
            // entity.setItemOptions(options);
        } else {
            // entity.setItemOptions(null);
        }

        return entity;
    }

    @Test
    @DisplayName("새로운 경매 기록이 있을 때 정상적으로 저장한다")
    void saveIfNotExists_WhenNewRecordsExist_ShouldSaveSuccessfully() {
        // given
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(filteredDtoList);
        when(mapper.toEntityList(filteredDtoList, category)).thenReturn(entities);

        // when
        auctionHistoryPersister.saveIfNotExists(dtoList, category);

        // then
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(filteredDtoList, category);
        verify(repository).saveAll(entities);
    }

    @Test
    @DisplayName("저장할 새로운 경매 기록이 없을 때 저장하지 않는다")
    void saveIfNotExists_WhenNoNewRecords_ShouldNotSave() {
        // given
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        auctionHistoryPersister.saveIfNotExists(dtoList, category);

        // then
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(Collections.emptyList(), category);
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("빈 DTO 리스트가 주어졌을 때 아무것도 저장하지 않는다")
    void saveIfNotExists_WhenEmptyDtoList_ShouldNotSave() {
        // given
        List<OpenApiAuctionHistoryResponse> emptyList = Collections.emptyList();
        when(duplicateChecker.filterExisting(emptyList)).thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        auctionHistoryPersister.saveIfNotExists(emptyList, category);

        // then
        verify(duplicateChecker).filterExisting(emptyList);
        verify(mapper).toEntityList(Collections.emptyList(), category);
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("모든 DTO가 중복일 때 저장하지 않는다")
    void saveIfNotExists_WhenAllDtosAreDuplicate_ShouldNotSave() {
        // given
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(Collections.emptyList());
        when(mapper.toEntityList(Collections.emptyList(), category))
                .thenReturn(Collections.emptyList());

        // when
        auctionHistoryPersister.saveIfNotExists(dtoList, category);

        // then
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(Collections.emptyList(), category);
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("다양한 카테고리에 대해 정상적으로 처리한다")
    void saveIfNotExists_WithDifferentCategories_ShouldProcessCorrectly() {
        // given
        ItemCategory armorCategory = ItemCategory.SWORD;
        when(duplicateChecker.filterExisting(dtoList)).thenReturn(filteredDtoList);
        when(mapper.toEntityList(filteredDtoList, armorCategory)).thenReturn(entities);

        // when
        auctionHistoryPersister.saveIfNotExists(dtoList, armorCategory);

        // then
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(filteredDtoList, armorCategory);
        verify(repository).saveAll(entities);
    }

    @Test
    @DisplayName("엔티티에 옵션이 null인 경우도 정상적으로 처리한다")
    void saveIfNotExists_WithNullOptions_ShouldProcessCorrectly() {
        // given
        AuctionHistory entityWithNullOptions = createMockAuctionHistory("item3", 0);
        List<AuctionHistory> entitiesWithNullOptions = Arrays.asList(entityWithNullOptions);

        when(duplicateChecker.filterExisting(dtoList)).thenReturn(filteredDtoList);
        when(mapper.toEntityList(filteredDtoList, category)).thenReturn(entitiesWithNullOptions);

        // when
        auctionHistoryPersister.saveIfNotExists(dtoList, category);

        // then
        verify(duplicateChecker).filterExisting(dtoList);
        verify(mapper).toEntityList(filteredDtoList, category);
        verify(repository).saveAll(entitiesWithNullOptions);
    }
}
