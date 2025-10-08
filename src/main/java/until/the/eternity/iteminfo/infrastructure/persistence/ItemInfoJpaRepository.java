package until.the.eternity.iteminfo.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

public interface ItemInfoJpaRepository
        extends JpaRepository<ItemInfo, String>, JpaSpecificationExecutor<ItemInfo> {

    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);
}
