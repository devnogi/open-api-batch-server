package until.the.eternity.iteminfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInfo {

    @Id
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sub_category", nullable = false)
    private String subCategory;

    @Column(name = "top_category", nullable = false)
    private String topCategory;

    @Column(name = "description")
    private String description;

    @Column(name = "inventory_width")
    private Byte inventoryWidth;

    @Column(name = "inventory_height")
    private Byte inventoryHeight;

    @Column(name = "inventory_max_bundle_count")
    private Integer inventoryMaxBundleCount;

    @Column(name = "history", length = 3000)
    private String history;

    @Column(name = "acquisition_method", length = 3000)
    private String acquisitionMethod;

    @Column(name = "store_sales_price", length = 3000)
    private String storeSalesPrice;

    @Column(name = "weapon_type")
    private String weaponType;

    @Column(name = "repair")
    private String repair;

    @Column(name = "max_alteration_count")
    private Byte maxAlterationCount;
}
