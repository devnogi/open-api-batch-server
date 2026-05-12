package until.the.eternity.iteminfo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;

import java.util.List;

public interface ItemInfoJpaRepository
        extends JpaRepository<ItemInfo, ItemInfoId>, JpaSpecificationExecutor<ItemInfo> {

    List<ItemInfo> findByIdTopCategory(String topCategory);

    List<ItemInfo> findByIdSubCategory(String subCategory);

    @Query("SELECT i.id FROM ItemInfo i")
    List<ItemInfoId> findAllIds();
}
