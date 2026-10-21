package until.the.eternity.auctionsearchoption.domain.repository;

import java.util.List;
import until.the.eternity.auctionsearchoption.domain.entity.AuctionSearchOptionMetadata;

public interface AuctionSearchOptionRepositoryPort {

    /**
     * 모든 활성화된 검색 옵션 조회 (정렬 순서대로)
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    List<AuctionSearchOptionMetadata> findAllActive();

    /**
     * 모든 검색 옵션 조회 (정렬 순서대로)
     *
     * @return 검색 옵션 메타데이터 리스트
     */
    List<AuctionSearchOptionMetadata> findAll();
}
