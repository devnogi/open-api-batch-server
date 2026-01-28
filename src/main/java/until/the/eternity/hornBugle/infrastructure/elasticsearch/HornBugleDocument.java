package until.the.eternity.hornBugle.infrastructure.elasticsearch;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;

@Document(indexName = "horn_bugle_world_history")
@Setting(settingPath = "elasticsearch/horn-bugle-settings.json")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HornBugleDocument {

    @Id private String id;

    @Field(type = FieldType.Keyword, name = "server_name")
    private String serverName;

    @Field(type = FieldType.Text, name = "character_name", analyzer = "nori_analyzer")
    private String characterName;

    @Field(type = FieldType.Text, name = "message", analyzer = "nori_analyzer")
    private String message;

    @Field(type = FieldType.Date, name = "date_send")
    private Instant dateSend;

    @Field(type = FieldType.Date, name = "date_register")
    private Instant dateRegister;

    public static HornBugleDocument from(HornBugleWorldHistory entity) {
        return HornBugleDocument.builder()
                .id(String.valueOf(entity.getId()))
                .serverName(entity.getServerName())
                .characterName(entity.getCharacterName())
                .message(entity.getMessage())
                .dateSend(entity.getDateSend())
                .dateRegister(entity.getDateRegister())
                .build();
    }
}
