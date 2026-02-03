package until.the.eternity.itemoptioninfo.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;
import until.the.eternity.itemoptioninfo.domain.repository.ItemOptionInfoRepositoryPort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemOptionInfoService {

    private final ItemOptionInfoRepositoryPort itemOptionInfoRepositoryPort;

    public List<ItemOptionInfo> findAll() {
        return itemOptionInfoRepositoryPort.findAll();
    }

    public ItemOptionInfo findById(ItemOptionInfoId id) {
        return itemOptionInfoRepositoryPort
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("아이템 옵션 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public ItemOptionInfo create(ItemOptionInfo itemOptionInfo) {
        if (itemOptionInfoRepositoryPort.existsById(itemOptionInfo.getId())) {
            throw new IllegalArgumentException("이미 존재하는 아이템 옵션 정보입니다.");
        }
        return itemOptionInfoRepositoryPort.save(itemOptionInfo);
    }

    @Transactional
    public ItemOptionInfo update(ItemOptionInfoId id, String optionDesc) {
        ItemOptionInfo itemOptionInfo = findById(id);
        itemOptionInfo.updateOptionDesc(optionDesc);
        return itemOptionInfoRepositoryPort.save(itemOptionInfo);
    }

    @Transactional
    public void delete(ItemOptionInfoId id) {
        ItemOptionInfo itemOptionInfo = findById(id);
        itemOptionInfoRepositoryPort.delete(itemOptionInfo);
    }
}
