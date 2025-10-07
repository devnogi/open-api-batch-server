package until.the.eternity.iteminfo.domain.repository;

import until.the.eternity.iteminfo.domain.entity.ItemInfo;

import java.util.List;

public interface ItemInfoRepositoryPort {
    List<ItemInfo> findAll();

    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);
}
