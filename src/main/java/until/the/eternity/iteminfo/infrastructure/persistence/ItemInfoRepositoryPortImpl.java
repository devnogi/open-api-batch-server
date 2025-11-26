package until.the.eternity.iteminfo.infrastructure.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;

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
        return jpaRepository.findByIdTopCategory(topCategory);
    }

    @Override
    public List<ItemInfo> findBySubCategory(String subCategory) {
        return jpaRepository.findByIdSubCategory(subCategory);
    }

    @Override
    public Page<ItemInfo> findAllWithPagination(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<ItemInfo> findAllSortedByName(Sort.Direction direction) {
        Sort sort = Sort.by(direction, "id.name");
        return jpaRepository.findAll(sort);
    }
}
