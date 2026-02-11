package until.the.eternity.metalwareinfo.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "metalware_attribute_info",
        uniqueConstraints = @UniqueConstraint(columnNames = {"metalware", "level"}))
@Getter
@NoArgsConstructor
public class MetalwareAttributeInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metalware", nullable = false, length = 50)
    private String metalware;

    @Column(name = "level")
    private Byte level;

    @Column(name = "attribute", length = 100)
    private String attribute;
}
