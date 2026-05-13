package until.the.eternity.hornBugle.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "horn_bugle_world_history",
        indexes = {
            @Index(
                    name = "idx_horn_bugle_server_date_send",
                    columnList = "server_name, date_send DESC"),
            @Index(name = "idx_horn_bugle_date_send", columnList = "date_send DESC"),
            @Index(name = "idx_horn_bugle_date_send_id", columnList = "date_send DESC, id DESC"),
            @Index(
                    name = "idx_horn_bugle_server_date_send_id",
                    columnList = "server_name, date_send DESC, id DESC")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HornBugleWorldHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "server_name", nullable = false, length = 20)
    private String serverName;

    @Column(name = "character_name", nullable = false, length = 100)
    private String characterName;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "date_send", nullable = false)
    private Instant dateSend;

    @Column(name = "date_register", nullable = false)
    private Instant dateRegister;
}
