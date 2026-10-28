package until.the.eternity.metalwareinfo.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metalware_info")
@Getter
@NoArgsConstructor
public class MetalwareInfoEntity {

    @Id
    @Column(name = "metalware", nullable = false, length = 50)
    private String metalware;
}
