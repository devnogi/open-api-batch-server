package until.the.eternity.totalsearch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "total_search_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TotalSearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Enumerated(EnumType.STRING)
    @Column(name = "keyword_type", nullable = false, length = 16)
    private TotalSearchKeywordType type;

    @Column(name = "target_key", nullable = false, length = 255)
    private String targetKey;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
