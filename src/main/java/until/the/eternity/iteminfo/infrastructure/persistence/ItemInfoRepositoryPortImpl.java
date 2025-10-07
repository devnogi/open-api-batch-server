package until.the.eternity.iteminfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemInfoRepositoryPortImpl implements ItemInfoRepositoryPort {
    private final ItemInfoJpaRepository jpaRepository;

    @Override
    public List<ItemInfo> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<ItemInfo> findByTopCategory(String topCategory) {
        return jpaRepository.findByTopCategory(topCategory);
    }

    @Override
    public List<ItemInfo> findBySubCategory(String subCategory) {
        return jpaRepository.findBySubCategory(subCategory);
    }
}
