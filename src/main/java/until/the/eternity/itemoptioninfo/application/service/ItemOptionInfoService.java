package until.the.eternity.itemoptioninfo.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.repository.ItemOptionInfoRepositoryPort;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemOptionInfoService {

    private final ItemOptionInfoRepositoryPort itemOptionInfoRepositoryPort;

    public List<ItemOptionInfo> findAll() {
        return itemOptionInfoRepositoryPort.findAll();
    }
}
