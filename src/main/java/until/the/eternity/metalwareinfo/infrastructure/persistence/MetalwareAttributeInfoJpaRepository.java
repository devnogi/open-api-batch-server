package until.the.eternity.metalwareinfo.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MetalwareAttributeInfoJpaRepository
        extends JpaRepository<MetalwareAttributeInfoEntity, Long> {

    @Modifying
    @Query(
            value =
                    """
            INSERT INTO metalware_attribute_info (metalware, level, attribute)
            SELECT DISTINCT
                option_value,
                CAST(option_value2 AS UNSIGNED),
                REGEXP_REPLACE(REGEXP_REPLACE(option_desc, '[()]', ''), '[0-9]+ ?레벨:? ?', '')
            FROM auction_history_item_option
            WHERE option_type = '세공 옵션'
              AND option_value2 IS NOT NULL AND option_value2 != ''
              AND REGEXP_REPLACE(REGEXP_REPLACE(option_desc, '[()]', ''), '[0-9]+ ?레벨:? ?', '') != ''
            ON DUPLICATE KEY UPDATE attribute = VALUES(attribute)
            """,
            nativeQuery = true)
    int syncFromAuctionHistory();

    Page<MetalwareAttributeInfoEntity> findByMetalwareContaining(
            String metalware, Pageable pageable);
}
