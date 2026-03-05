package until.the.eternity.enchantinfo.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enchant_info")
@Getter
@NoArgsConstructor
public class EnchantInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fullname", nullable = false, length = 100)
    private String fullname;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "enchant_rank", nullable = false, length = 1)
    private String enchantRank;

    @Column(name = "affix_position", nullable = false, length = 10)
    private String affixPosition;

    @Column(name = "effect", length = 100)
    private String effect;

    @Column(name = "acquired_info", length = 200)
    private String acquiredInfo;

    @Column(name = "full_option_rate")
    private Integer fullOptionRate;

    @Column(name = "is_exclusive")
    private Boolean isExclusive;

    @Column(name = "is_common_part")
    private Boolean isCommonPart;
}
