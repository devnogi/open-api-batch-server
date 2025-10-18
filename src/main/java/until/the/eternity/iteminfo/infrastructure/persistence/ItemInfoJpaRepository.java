package until.the.eternity.iteminfo.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;

public interface ItemInfoJpaRepository
        extends JpaRepository<ItemInfo, ItemInfoId>, JpaSpecificationExecutor<ItemInfo> {

    List<ItemInfo> findByIdTopCategory(String topCategory);

    List<ItemInfo> findByIdSubCategory(String subCategory);
}
