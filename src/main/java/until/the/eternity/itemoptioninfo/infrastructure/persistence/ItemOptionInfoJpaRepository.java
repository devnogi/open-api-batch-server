package until.the.eternity.itemoptioninfo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;

public interface ItemOptionInfoJpaRepository
        extends JpaRepository<ItemOptionInfo, ItemOptionInfoId> {}
