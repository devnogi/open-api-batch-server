package until.the.eternity.itemoptioninfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;
import until.the.eternity.itemoptioninfo.domain.repository.ItemOptionInfoRepositoryPort;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ItemOptionInfoRepositoryPortImpl implements ItemOptionInfoRepositoryPort {

    private final ItemOptionInfoJpaRepository itemOptionInfoJpaRepository;

    @Override
    public List<ItemOptionInfo> findAll() {
        return itemOptionInfoJpaRepository.findAll();
    }

    @Override
    public Optional<ItemOptionInfo> findById(ItemOptionInfoId id) {
        return itemOptionInfoJpaRepository.findById(id);
    }

    @Override
    public ItemOptionInfo save(ItemOptionInfo itemOptionInfo) {
        return itemOptionInfoJpaRepository.save(itemOptionInfo);
    }

    @Override
    public void delete(ItemOptionInfo itemOptionInfo) {
        itemOptionInfoJpaRepository.delete(itemOptionInfo);
    }

    @Override
    public boolean existsById(ItemOptionInfoId id) {
        return itemOptionInfoJpaRepository.existsById(id);
    }
}
