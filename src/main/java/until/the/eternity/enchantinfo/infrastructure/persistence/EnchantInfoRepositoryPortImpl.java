package until.the.eternity.enchantinfo.infrastructure.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import until.the.eternity.enchantinfo.domain.repository.EnchantInfoRepositoryPort;

@Repository
@RequiredArgsConstructor
public class EnchantInfoRepositoryPortImpl implements EnchantInfoRepositoryPort {

    private final EnchantInfoJpaRepository jpaRepository;

    @Override
    public Page<EnchantInfoEntity> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable);
    }

    @Override
    public List<String> findAllFullnames() {
        return jpaRepository.findAllFullnames();
    }

    @Override
    public List<String> findAllFullnamesByAffixPosition(String affixPosition) {
        return jpaRepository.findAllFullnamesByAffixPosition(affixPosition);
    }

    @Override
    public int upsertFromAuctionHistory() {
        return jpaRepository.upsertFromAuctionHistory();
    }
}
