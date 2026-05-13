package until.the.eternity.enchantinfo.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import until.the.eternity.enchantinfo.infrastructure.persistence.EnchantInfoEntity;

import java.util.List;

public interface EnchantInfoRepositoryPort {

    Page<EnchantInfoEntity> findAll(Pageable pageable);

    List<String> findAllFullnames();

    List<String> findAllFullnamesByAffixPosition(String affixPosition);

    int upsertFromAuctionHistory();
}
