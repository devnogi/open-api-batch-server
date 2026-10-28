package until.the.eternity.hornBugle.kafka.dto;

import java.time.Instant;

public record UserVerificationVerifyEvent(
        String characterName,
        String serverName,
        String verificationValue,
        String message,
        Instant dateSend) {}
