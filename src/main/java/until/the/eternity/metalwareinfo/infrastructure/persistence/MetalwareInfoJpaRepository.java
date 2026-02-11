package until.the.eternity.metalwareinfo.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MetalwareInfoJpaRepository extends JpaRepository<MetalwareInfoEntity, String> {

    @Query("SELECT m.metalware FROM MetalwareInfoEntity m")
    List<String> findAllMetalwares();

    @Modifying
    @Query(
            value =
                    """
            INSERT INTO metalware_info (metalware, level_attribute)
            SELECT
                metalware,
                attribute
            FROM metalware_attribute_info
            WHERE level = 1
            ON DUPLICATE KEY UPDATE level_attribute = VALUES(level_attribute)
            """,
            nativeQuery = true)
    int upsertLevelAttributeFromAttributeInfo();

    @Modifying
    @Query(
            value =
                    """
            INSERT INTO metalware_info (metalware, limit_break_level)
            SELECT
                metalware,
                MAX(level) AS limit_break_level
            FROM metalware_attribute_info
            GROUP BY metalware
            ON DUPLICATE KEY UPDATE limit_break_level = VALUES(limit_break_level)
            """,
            nativeQuery = true)
    int upsertLimitBreakLevelFromAttributeInfo();
}
