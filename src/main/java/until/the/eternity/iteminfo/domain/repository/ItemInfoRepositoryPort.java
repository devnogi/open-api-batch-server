package until.the.eternity.iteminfo.domain.repository;

import java.util.List;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

public interface ItemInfoRepositoryPort {
    List<ItemInfo> findAll();

    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);
}
