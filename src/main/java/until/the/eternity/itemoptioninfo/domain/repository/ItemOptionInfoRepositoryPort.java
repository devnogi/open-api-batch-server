package until.the.eternity.itemoptioninfo.domain.repository;

import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;

import java.util.List;

public interface ItemOptionInfoRepositoryPort {
    List<ItemOptionInfo> findAll();
}
