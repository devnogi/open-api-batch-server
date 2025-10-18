package until.the.eternity.iteminfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
