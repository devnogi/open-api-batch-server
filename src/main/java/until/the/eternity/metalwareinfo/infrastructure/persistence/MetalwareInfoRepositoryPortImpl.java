package until.the.eternity.metalwareinfo.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import until.the.eternity.metalwareinfo.domain.repository.MetalwareInfoRepositoryPort;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MetalwareInfoRepositoryPortImpl implements MetalwareInfoRepositoryPort {
    private final MetalwareInfoJpaRepository jpaRepository;

    @Override
    public List<String> findAllMetalwares() {
        return jpaRepository.findAllMetalwares();
    }
}
