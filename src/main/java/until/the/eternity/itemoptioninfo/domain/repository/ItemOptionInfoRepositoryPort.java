package until.the.eternity.itemoptioninfo.domain.repository;

import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;

import java.util.List;
import java.util.Optional;

public interface ItemOptionInfoRepositoryPort {
    List<ItemOptionInfo> findAll();

    Optional<ItemOptionInfo> findById(ItemOptionInfoId id);

    ItemOptionInfo save(ItemOptionInfo itemOptionInfo);

    void delete(ItemOptionInfo itemOptionInfo);

    boolean existsById(ItemOptionInfoId id);
}
