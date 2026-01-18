package until.the.eternity.itemoptioninfo.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.repository.ItemOptionInfoRepositoryPort;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemOptionInfoService {

    private final ItemOptionInfoRepositoryPort itemOptionInfoRepositoryPort;

    public List<ItemOptionInfo> findAll() {
        return itemOptionInfoRepositoryPort.findAll();
    }
}
