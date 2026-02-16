package until.the.eternity.hornBugle.kafka.application;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import until.the.eternity.common.constant.KafkaTopicConstant;
import until.the.eternity.hornBugle.kafka.dto.UserVerificationVerifyEvent;

@Service
@RequiredArgsConstructor
public class HornBugleKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value(
            "${app.kafka.topics.user-verification-verify:"
                    + KafkaTopicConstant.USER_VERIFICATION_VERIFY_EVENT
                    + "}")
    private String userVerificationVerifyTopic;

    public void sendUserVerificationVerifyEvent(UserVerificationVerifyEvent event) {
        kafkaTemplate.send(userVerificationVerifyTopic, event);
    }
}
