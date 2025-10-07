package until.the.eternity.iteminfo.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepository;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemInfoService {

    private final ItemInfoRepository itemInfoRepository;

    public List<ItemInfoResponse> findAll() {
        List<ItemInfo> itemInfos = itemInfoRepository.findAll();
        return ItemInfoResponse.from(itemInfos);
    }

    public List<ItemInfoResponse> findByItemName(String name) {
        List<ItemInfo> itemInfos = itemInfoRepository.findByNameContaining(name);
        return ItemInfoResponse.from(itemInfos);
    }

    public List<ItemInfoResponse> findByTopCategory(String topCategory) {
        List<ItemInfo> itemInfos = itemInfoRepository.findByTopCategory(topCategory);
        return ItemInfoResponse.from(itemInfos);
    }

    public List<ItemInfoResponse> findBySubCategory(String subCategory) {
        List<ItemInfo> itemInfos = itemInfoRepository.findBySubCategory(subCategory);
        return ItemInfoResponse.from(itemInfos);
    }
}
