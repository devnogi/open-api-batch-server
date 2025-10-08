package until.the.eternity.itemoptioninfo.domain.repository;

import java.util.List;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;

public interface ItemOptionInfoRepositoryPort {
    List<ItemOptionInfo> findAll();
}
