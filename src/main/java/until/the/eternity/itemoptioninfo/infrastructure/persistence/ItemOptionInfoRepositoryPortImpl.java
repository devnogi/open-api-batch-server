package until.the.eternity.itemoptioninfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.repository.ItemOptionInfoRepositoryPort;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemOptionInfoRepositoryPortImpl implements ItemOptionInfoRepositoryPort {

    private final ItemOptionInfoJpaRepository itemOptionInfoJpaRepository;

    @Override
    public List<ItemOptionInfo> findAll() {
        return itemOptionInfoJpaRepository.findAll();
    }
}
