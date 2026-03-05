package until.the.eternity.enchantinfo.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.config.CacheNames;
import until.the.eternity.enchantinfo.domain.exception.EnchantInfoExceptionCode;
import until.the.eternity.enchantinfo.domain.repository.EnchantInfoRepositoryPort;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoResponse;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoSyncResponse;

import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnchantInfoService {

    private static final Set<String> ALLOWED_AFFIX_POSITIONS = Set.of("접두", "접미");

    private final EnchantInfoRepositoryPort enchantInfoRepository;

    @Cacheable(
            cacheNames = CacheNames.ENCHANT_INFO_ALL,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildEnchantInfoAllKey(#pageable)")
    public Page<EnchantInfoResponse> findAll(Pageable pageable) {
        return enchantInfoRepository.findAll(pageable).map(EnchantInfoResponse::from);
    }

    @Cacheable(
            cacheNames = CacheNames.ENCHANT_INFO_FULLNAMES,
            key =
                    "T(until.the.eternity.common.util.CacheKeyBuilder)"
                            + ".buildEnchantInfoFullnamesKey(#affixPosition)")
    public List<String> findAllFullnames(String affixPosition) {
        if (affixPosition != null) {
            if (!ALLOWED_AFFIX_POSITIONS.contains(affixPosition)) {
                throw new CustomException(EnchantInfoExceptionCode.INVALID_AFFIX_POSITION);
            }
            return enchantInfoRepository.findAllFullnamesByAffixPosition(affixPosition);
        }
        return enchantInfoRepository.findAllFullnames();
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = CacheNames.ENCHANT_INFO_ALL, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.ENCHANT_INFO_FULLNAMES, allEntries = true)
            })
    @Transactional
    public EnchantInfoSyncResponse syncFromAuctionHistory() {
        int upserted = enchantInfoRepository.upsertFromAuctionHistory();
        return new EnchantInfoSyncResponse(upserted);
    }
}
