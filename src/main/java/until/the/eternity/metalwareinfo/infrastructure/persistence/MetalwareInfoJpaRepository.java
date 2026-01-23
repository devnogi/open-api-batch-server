package until.the.eternity.metalwareinfo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MetalwareInfoJpaRepository extends JpaRepository<MetalwareInfoEntity, String> {

    @Query("SELECT m.metalware FROM MetalwareInfoEntity m")
    List<String> findAllMetalwares();
}
