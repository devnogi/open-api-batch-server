package until.the.eternity.iteminfo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.domain.exception.ItemInfoExceptionCode;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemCategoryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSyncResponse;

@ExtendWith(MockitoExtension.class)
class ItemInfoServiceTest {

    @Mock private ItemInfoRepositoryPort itemInfoRepository;

    @Mock private AuctionHistoryRepositoryPort auctionHistoryRepository;

    @InjectMocks private ItemInfoService itemInfoService;

    @Test
    @DisplayName("모든 아이템 카테고리를 조회하면 카테고리 목록을 반환한다")
    void findItemCategories_should_return_all_categories() {
        // when
        List<ItemCategoryResponse> result = itemInfoService.findItemCategories();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).extracting("subCategory").contains("한손 장비", "검", "활");
        assertThat(result).extracting("topCategory").contains("근거리 장비", "원거리 장비");
    }

    @Test
    @DisplayName("모든 아이템 정보를 조회하면 아이템 목록을 반환한다")
    void findAll_should_return_all_items() {
        // given
        ItemInfo item1 = createItemInfo("나뭇가지", "한손검", "무기");
        ItemInfo item2 = createItemInfo("숏소드", "한손검", "무기");
        when(itemInfoRepository.findAll()).thenReturn(List.of(item1, item2));

        // when
        List<ItemInfoResponse> result = itemInfoService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("나뭇가지", "숏소드");
        verify(itemInfoRepository).findAll();
    }

    @Test
    @DisplayName("상위 카테고리로 아이템을 조회하면 해당 카테고리의 아이템 목록을 반환한다")
    void findByTopCategory_should_return_items_by_top_category() {
        // given
        String topCategory = "소모품";
        ItemInfo item1 = createItemInfo("염색 앰플", "염색 앰플", topCategory);
        ItemInfo item2 = createItemInfo("지정 색상 염색 앰플", "염색 앰플", topCategory);
        when(itemInfoRepository.findByTopCategory(topCategory)).thenReturn(List.of(item1, item2));

        // when
        List<ItemInfoResponse> result = itemInfoService.findByTopCategory(topCategory);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("topCategory").containsOnly("소모품");
        assertThat(result).extracting("name").containsExactly("염색 앰플", "지정 색상 염색 앰플");
        verify(itemInfoRepository).findByTopCategory(topCategory);
    }

    @Test
    @DisplayName("하위 카테고리로 아이템을 조회하면 해당 카테고리의 아이템 목록을 반환한다")
    void findBySubCategory_should_return_items_by_sub_category() {
        // given
        String topCategory = "소모품";
        String subCategory = "염색 앰플";
        ItemInfo item1 = createItemInfo("염색 앰플", subCategory, topCategory);
        ItemInfo item2 = createItemInfo("지정 색상 염색 앰플", subCategory, topCategory);
        when(itemInfoRepository.findBySubCategory(subCategory)).thenReturn(List.of(item1, item2));

        // when
        List<ItemInfoResponse> result = itemInfoService.findBySubCategory(subCategory);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("subCategory").containsOnly("염색 앰플");
        assertThat(result).extracting("name").containsExactly("염색 앰플", "지정 색상 염색 앰플");
        verify(itemInfoRepository).findBySubCategory(subCategory);
    }

    @Test
    @DisplayName("아이템 정보가 없으면 빈 목록을 반환한다")
    void findAll_should_return_empty_list_when_no_data() {
        // given
        when(itemInfoRepository.findAll()).thenReturn(List.of());

        // when
        List<ItemInfoResponse> result = itemInfoService.findAll();

        // then
        assertThat(result).isEmpty();
        verify(itemInfoRepository).findAll();
    }

    @Test
    @DisplayName("상세 정보 조회 시 topCategory가 없으면 예외가 발생한다")
    void findAllDetail_should_throw_exception_when_topCategory_is_null() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, null);
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() -> itemInfoService.findAllDetail(searchRequest, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ItemInfoExceptionCode.TOP_CATEGORY_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("상세 정보 조회 시 topCategory가 빈 문자열이면 예외가 발생한다")
    void findAllDetail_should_throw_exception_when_topCategory_is_blank() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "");
        Pageable pageable = PageRequest.of(0, 20);

        // when & then
        assertThatThrownBy(() -> itemInfoService.findAllDetail(searchRequest, pageable))
                .isInstanceOf(CustomException.class)
                .hasMessage(ItemInfoExceptionCode.TOP_CATEGORY_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("상세 정보 조회 시 topCategory가 있으면 정상적으로 조회된다")
    void findAllDetail_should_return_items_when_topCategory_is_present() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "무기");
        Pageable pageable = PageRequest.of(0, 20);

        ItemInfo item1 = createItemInfo("나뭇가지", "한손검", "무기");
        ItemInfo item2 = createItemInfo("숏소드", "한손검", "무기");
        Page<ItemInfo> itemInfoPage = new PageImpl<>(List.of(item1, item2), pageable, 2);

        when(itemInfoRepository.searchWithPagination(searchRequest, pageable))
                .thenReturn(itemInfoPage);

        // when
        Page<ItemInfoResponse> result = itemInfoService.findAllDetail(searchRequest, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("name").containsExactly("나뭇가지", "숏소드");
        verify(itemInfoRepository).searchWithPagination(searchRequest, pageable);
    }

    @Test
    @DisplayName("상세 정보 조회 시 topCategory와 name, subCategory를 모두 사용하면 필터링된 결과를 반환한다")
    void findAllDetail_should_return_filtered_items_with_all_conditions() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest("나뭇가지", "한손검", "무기");
        Pageable pageable = PageRequest.of(0, 20);

        ItemInfo item = createItemInfo("나뭇가지", "한손검", "무기");
        Page<ItemInfo> itemInfoPage = new PageImpl<>(List.of(item), pageable, 1);

        when(itemInfoRepository.searchWithPagination(searchRequest, pageable))
                .thenReturn(itemInfoPage);

        // when
        Page<ItemInfoResponse> result = itemInfoService.findAllDetail(searchRequest, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("나뭇가지");
        assertThat(result.getContent().get(0).subCategory()).isEqualTo("한손검");
        assertThat(result.getContent().get(0).topCategory()).isEqualTo("무기");
        verify(itemInfoRepository).searchWithPagination(searchRequest, pageable);
    }

    @Test
    @DisplayName("요약 정보 조회 시 topCategory가 없으면 예외가 발생한다")
    void findAllSummary_should_throw_exception_when_topCategory_is_null() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, null);

        // when & then
        assertThatThrownBy(() -> itemInfoService.findAllSummary(searchRequest, Sort.Direction.ASC))
                .isInstanceOf(CustomException.class)
                .hasMessage(ItemInfoExceptionCode.TOP_CATEGORY_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("요약 정보 조회 시 topCategory가 있으면 정상적으로 조회된다")
    void findAllSummary_should_return_items_when_topCategory_is_present() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "무기");
        ItemInfo item1 = createItemInfo("나뭇가지", "한손검", "무기");
        ItemInfo item2 = createItemInfo("숏소드", "한손검", "무기");

        when(itemInfoRepository.search(eq(searchRequest), any(Pageable.class)))
                .thenReturn(List.of(item1, item2));

        // when
        List<ItemInfoSummaryResponse> result =
                itemInfoService.findAllSummary(searchRequest, Sort.Direction.ASC);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("나뭇가지", "숏소드");
        assertThat(result).extracting("topCategory").containsOnly("무기");
        verify(itemInfoRepository).search(eq(searchRequest), any(Pageable.class));
    }

    @Test
    @DisplayName("아이템 동기화 시 중복되지 않은 아이템만 저장된다")
    void syncItemInfoFromAuctionHistory_should_save_only_new_items() {
        // given
        Object[] item1 = {"나뭇가지", "무기", "한손검"};
        Object[] item2 = {"숏소드", "무기", "한손검"};
        Object[] item3 = {"염색 앰플", "소모품", "염색 앰플"};

        when(auctionHistoryRepository.findDistinctItemInfo())
                .thenReturn(List.of(item1, item2, item3));

        // 나뭇가지만 이미 존재
        ItemInfoId existingId = new ItemInfoId("나뭇가지", "한손검", "무기");
        when(itemInfoRepository.findAllIds()).thenReturn(List.of(existingId));

        // when
        ItemInfoSyncResponse result = itemInfoService.syncItemInfoFromAuctionHistory();

        // then
        assertThat(result.syncedItemNames()).hasSize(2);
        assertThat(result.syncedItemNames()).containsExactly("숏소드", "염색 앰플");
        assertThat(result.syncedCount()).isEqualTo(2);
        verify(itemInfoRepository).saveAll(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("아이템 동기화 시 모든 아이템이 이미 존재하면 아무것도 저장하지 않는다")
    void syncItemInfoFromAuctionHistory_should_not_save_when_all_items_exist() {
        // given
        Object[] item1 = new Object[] {"나뭇가지", "무기", "한손검"};
        List<Object[]> distinctItems = new ArrayList<>();
        distinctItems.add(item1);

        when(auctionHistoryRepository.findDistinctItemInfo()).thenReturn(distinctItems);

        // 모든 아이템이 이미 존재
        ItemInfoId existingId = new ItemInfoId("나뭇가지", "한손검", "무기");
        when(itemInfoRepository.findAllIds()).thenReturn(List.of(existingId));

        // when
        ItemInfoSyncResponse result = itemInfoService.syncItemInfoFromAuctionHistory();

        // then
        assertThat(result.syncedItemNames()).isEmpty();
        assertThat(result.syncedCount()).isZero();
        verify(itemInfoRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("아이템 동기화 시 AuctionHistory에 데이터가 없으면 빈 결과를 반환한다")
    void syncItemInfoFromAuctionHistory_should_return_empty_when_no_data_in_auction_history() {
        // given
        when(auctionHistoryRepository.findDistinctItemInfo()).thenReturn(new ArrayList<>());
        when(itemInfoRepository.findAllIds()).thenReturn(new ArrayList<>());

        // when
        ItemInfoSyncResponse result = itemInfoService.syncItemInfoFromAuctionHistory();

        // then
        assertThat(result.syncedItemNames()).isEmpty();
        assertThat(result.syncedCount()).isZero();
        verify(itemInfoRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("상위 카테고리로 조회 시 결과가 없으면 빈 목록을 반환한다")
    void findByTopCategory_should_return_empty_list_when_no_results() {
        // given
        String topCategory = "존재하지않는카테고리";
        when(itemInfoRepository.findByTopCategory(topCategory)).thenReturn(List.of());

        // when
        List<ItemInfoResponse> result = itemInfoService.findByTopCategory(topCategory);

        // then
        assertThat(result).isEmpty();
        verify(itemInfoRepository).findByTopCategory(topCategory);
    }

    @Test
    @DisplayName("하위 카테고리로 조회 시 결과가 없으면 빈 목록을 반환한다")
    void findBySubCategory_should_return_empty_list_when_no_results() {
        // given
        String subCategory = "존재하지않는카테고리";
        when(itemInfoRepository.findBySubCategory(subCategory)).thenReturn(List.of());

        // when
        List<ItemInfoResponse> result = itemInfoService.findBySubCategory(subCategory);

        // then
        assertThat(result).isEmpty();
        verify(itemInfoRepository).findBySubCategory(subCategory);
    }

    @Test
    @DisplayName("요약 정보 조회 시 topCategory가 빈 문자열이면 예외가 발생한다")
    void findAllSummary_should_throw_exception_when_topCategory_is_blank() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "");

        // when & then
        assertThatThrownBy(() -> itemInfoService.findAllSummary(searchRequest, Sort.Direction.ASC))
                .isInstanceOf(CustomException.class)
                .hasMessage(ItemInfoExceptionCode.TOP_CATEGORY_REQUIRED.getMessage());
    }

    @Test
    @DisplayName("요약 정보 조회 시 결과가 없으면 빈 목록을 반환한다")
    void findAllSummary_should_return_empty_list_when_no_results() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "무기");
        when(itemInfoRepository.search(eq(searchRequest), any(Pageable.class)))
                .thenReturn(List.of());

        // when
        List<ItemInfoSummaryResponse> result =
                itemInfoService.findAllSummary(searchRequest, Sort.Direction.ASC);

        // then
        assertThat(result).isEmpty();
        verify(itemInfoRepository).search(eq(searchRequest), any(Pageable.class));
    }

    @Test
    @DisplayName("요약 정보 조회 시 내림차순 정렬이 적용된다")
    void findAllSummary_should_apply_descending_sort() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "무기");
        ItemInfo item1 = createItemInfo("A아이템", "한손검", "무기");
        ItemInfo item2 = createItemInfo("Z아이템", "한손검", "무기");

        when(itemInfoRepository.search(eq(searchRequest), any(Pageable.class)))
                .thenReturn(List.of(item2, item1));

        // when
        List<ItemInfoSummaryResponse> result =
                itemInfoService.findAllSummary(searchRequest, Sort.Direction.DESC);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactly("Z아이템", "A아이템");
        verify(itemInfoRepository).search(eq(searchRequest), any(Pageable.class));
    }

    @Test
    @DisplayName("상세 정보 조회 시 결과가 없으면 빈 페이지를 반환한다")
    void findAllDetail_should_return_empty_page_when_no_results() {
        // given
        ItemInfoSearchRequest searchRequest = new ItemInfoSearchRequest(null, null, "무기");
        Pageable pageable = PageRequest.of(0, 20);
        Page<ItemInfo> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(itemInfoRepository.searchWithPagination(searchRequest, pageable))
                .thenReturn(emptyPage);

        // when
        Page<ItemInfoResponse> result = itemInfoService.findAllDetail(searchRequest, pageable);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        verify(itemInfoRepository).searchWithPagination(searchRequest, pageable);
    }

    private ItemInfo createItemInfo(String name, String subCategory, String topCategory) {
        ItemInfo itemInfo = mock(ItemInfo.class);

        when(itemInfo.getName()).thenReturn(name);
        when(itemInfo.getSubCategory()).thenReturn(subCategory);
        when(itemInfo.getTopCategory()).thenReturn(topCategory);

        return itemInfo;
    }
}
