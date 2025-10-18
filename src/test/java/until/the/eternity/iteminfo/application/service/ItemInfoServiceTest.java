package until.the.eternity.iteminfo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemCategoryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;

@ExtendWith(MockitoExtension.class)
class ItemInfoServiceTest {

    @Mock private ItemInfoRepositoryPort itemInfoRepository;

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

    private ItemInfo createItemInfo(String name, String subCategory, String topCategory) {
        ItemInfo itemInfo = mock(ItemInfo.class);

        when(itemInfo.getName()).thenReturn(name);
        when(itemInfo.getSubCategory()).thenReturn(subCategory);
        when(itemInfo.getTopCategory()).thenReturn(topCategory);

        return itemInfo;
    }
}
