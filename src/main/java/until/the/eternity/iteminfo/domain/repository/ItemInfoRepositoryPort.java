package until.the.eternity.iteminfo.domain.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

public interface ItemInfoRepositoryPort {
    List<ItemInfo> findAll();

    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);

    Page<ItemInfo> findAllWithPagination(Pageable pageable);

    List<ItemInfo> findAllSortedByName(org.springframework.data.domain.Sort.Direction direction);
}
