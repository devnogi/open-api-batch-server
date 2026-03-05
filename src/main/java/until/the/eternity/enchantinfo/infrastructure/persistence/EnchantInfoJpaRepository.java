package until.the.eternity.enchantinfo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnchantInfoJpaRepository extends JpaRepository<EnchantInfoEntity, Long> {

    @Query("SELECT e.fullname FROM EnchantInfoEntity e ORDER BY e.id ASC")
    List<String> findAllFullnames();

    @Query(
            "SELECT e.fullname FROM EnchantInfoEntity e WHERE e.affixPosition = :affixPosition ORDER BY e.id ASC")
    List<String> findAllFullnamesByAffixPosition(
            @org.springframework.data.repository.query.Param("affixPosition") String affixPosition);

    @Modifying
    @Query(
            value =
                    """
            INSERT INTO enchant_info (fullname, name, enchant_rank, affix_position)
            SELECT DISTINCT
                option_value,
                REGEXP_REPLACE(option_value, ' ?[(]랭크.*', '') AS name,
                REGEXP_SUBSTR(REGEXP_SUBSTR(option_value, '랭크 [A-Za-z0-9]', 1, 1), '[A-Za-z0-9]+', 1, 1),
                option_sub_type
            FROM auction_history_item_option
            WHERE option_type = '인챈트'
            ON DUPLICATE KEY UPDATE fullname = VALUES(fullname)
            """,
            nativeQuery = true)
    int upsertFromAuctionHistory();
}
