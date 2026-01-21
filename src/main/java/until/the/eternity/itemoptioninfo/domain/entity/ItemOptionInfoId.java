package until.the.eternity.itemoptioninfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemOptionInfoId implements Serializable {

    @Column(name = "option_type", length = 100)
    private String optionType;

    @Column(name = "option_sub_type", length = 100)
    private String optionSubType;

    @Column(name = "option_value", length = 255)
    private String optionValue;

    @Column(name = "option_value2", length = 255)
    private String optionValue2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemOptionInfoId that = (ItemOptionInfoId) o;
        return Objects.equals(optionType, that.optionType)
                && Objects.equals(optionSubType, that.optionSubType)
                && Objects.equals(optionValue, that.optionValue)
                && Objects.equals(optionValue2, that.optionValue2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(optionType, optionSubType, optionValue, optionValue2);
    }
}
