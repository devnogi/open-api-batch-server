package until.the.eternity.enchantinfo.application.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.enchantinfo.domain.exception.EnchantInfoExceptionCode;
import until.the.eternity.enchantinfo.domain.repository.EnchantInfoRepositoryPort;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoResponse;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoSyncResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnchantInfoService {

    private static final Set<String> ALLOWED_AFFIX_POSITIONS = Set.of("접두", "접미");

    private final EnchantInfoRepositoryPort enchantInfoRepository;

    public Page<EnchantInfoResponse> findAll(Pageable pageable) {
        return enchantInfoRepository.findAll(pageable).map(EnchantInfoResponse::from);
    }

    public List<String> findAllFullnames(String affixPosition) {
        if (affixPosition != null) {
            if (!ALLOWED_AFFIX_POSITIONS.contains(affixPosition)) {
                throw new CustomException(EnchantInfoExceptionCode.INVALID_AFFIX_POSITION);
            }
            return enchantInfoRepository.findAllFullnamesByAffixPosition(affixPosition);
        }
        return enchantInfoRepository.findAllFullnames();
    }

    @Transactional
    public EnchantInfoSyncResponse syncFromAuctionHistory() {
        int upserted = enchantInfoRepository.upsertFromAuctionHistory();
        return new EnchantInfoSyncResponse(upserted);
    }
}
