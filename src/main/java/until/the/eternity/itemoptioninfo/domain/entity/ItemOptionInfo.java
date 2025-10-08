package until.the.eternity.itemoptioninfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_option_value_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOptionInfo {

    @EmbeddedId private ItemOptionInfoId id;

    @Column(name = "option_desc", columnDefinition = "text")
    private String optionDesc;
}
