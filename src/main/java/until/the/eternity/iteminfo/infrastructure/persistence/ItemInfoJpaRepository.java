package until.the.eternity.iteminfo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;

import java.util.List;

public interface ItemInfoJpaRepository
        extends JpaRepository<ItemInfo, String>, JpaSpecificationExecutor<ItemInfo> {


    List<ItemInfo> findByTopCategory(String topCategory);

    List<ItemInfo> findBySubCategory(String subCategory);
}
