package until.the.eternity.iteminfo.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

public interface ItemInfoRepository extends JpaRepository<ItemInfo, String> {

    List<ItemInfo> findByNameContaining(String name);

    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);
}
