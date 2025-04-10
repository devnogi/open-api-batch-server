package until.the.eternity.hornBugle.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "horn_bugle_world_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HornBugleWorldHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 고유 식별자

    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName; // 발화한 유저의 캐릭터명

    @Column(name = "message", nullable = false)
    private String message; // 발화한 거대한 외침의 뿔피리 내용

    @Column(name = "date_send", nullable = false)
    private LocalDateTime dateSend; // 발화한 시각 (UTC0)
}
