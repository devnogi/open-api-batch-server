package until.the.eternity.iteminfo.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ItemInfo {

    @EmbeddedId private ItemInfoId id;

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

    // Helper methods for backward compatibility
    public String getName() {
        return id != null ? id.getName() : null;
    }

    public String getSubCategory() {
        return id != null ? id.getSubCategory() : null;
    }

    public String getTopCategory() {
        return id != null ? id.getTopCategory() : null;
    }
}
