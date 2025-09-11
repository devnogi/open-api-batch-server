package until.the.eternity.item.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.item.interfaces.rest.dto.ItemCategoryResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    public List<ItemCategoryResponse> findItemCategories() {
        return ItemCategoryResponse.from();
    }
}
