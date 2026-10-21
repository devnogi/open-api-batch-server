package until.the.eternity.enchantinfo.domain.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.enchantinfo.infrastructure.persistence.EnchantInfoEntity;

public interface EnchantInfoRepositoryPort {

    Page<EnchantInfoEntity> findAll(Pageable pageable);

    List<String> findAllFullnames();

    List<String> findAllFullnamesByAffixPosition(String affixPosition);

    int upsertFromAuctionHistory();
}
