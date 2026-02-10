package until.the.eternity.iteminfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class ItemInfoId implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;

    @Column(name = "top_category", nullable = false)
    private String topCategory;
}
