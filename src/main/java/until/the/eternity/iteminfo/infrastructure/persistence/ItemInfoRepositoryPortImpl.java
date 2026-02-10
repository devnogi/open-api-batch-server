package until.the.eternity.iteminfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemInfoRepositoryPortImpl implements ItemInfoRepositoryPort {
    private final ItemInfoJpaRepository jpaRepository;
    private final ItemInfoQueryDslRepository queryDslRepository;

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

    @Override
    public boolean existsById(until.the.eternity.iteminfo.domain.entity.ItemInfoId id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<until.the.eternity.iteminfo.domain.entity.ItemInfoId> findAllIds() {
        return jpaRepository.findAllIds();
    }

    @Override
    public void saveAll(List<ItemInfo> itemInfos) {
        jpaRepository.saveAll(itemInfos);
    }

    @Override
    public Page<ItemInfo> searchWithPagination(
            ItemInfoSearchRequest searchRequest, Pageable pageable) {
        return queryDslRepository.searchWithPagination(searchRequest, pageable);
    }

    @Override
    public List<ItemInfo> search(ItemInfoSearchRequest searchRequest, Pageable pageable) {
        return queryDslRepository.search(searchRequest, pageable);
    }
}
