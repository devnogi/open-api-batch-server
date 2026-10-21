package until.the.eternity.hornBugle.kafka.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JsonSerializer;

class UserVerificationContractTest {
    @Test
    void kafkaPayloadMatchesPublishedFieldsAndFixture() throws Exception {
        var event =
                new UserVerificationVerifyEvent(
                        "contract-character",
                        "류트",
                        "fixture-only-token",
                        "synthetic contract fixture",
                        Instant.parse("2026-01-01T00:00:00Z"));
        try (var serializer = new JsonSerializer<UserVerificationVerifyEvent>()) {
            var mapper = new ObjectMapper().findAndRegisterModules();
            JsonNode payload = mapper.readTree(serializer.serialize("contract-test", event));
            JsonNode schema =
                    mapper.readTree(
                            Files.readString(
                                    Path.of("contracts/user-verification-verify.schema.json")));
            JsonNode fixture =
                    mapper.readTree(
                            Files.readString(
                                    Path.of("contracts/user-verification-verify.example.json")));
            Set<String> declared = new HashSet<>();
            schema.path("properties").fieldNames().forEachRemaining(declared::add);
            Set<String> actual = new HashSet<>();
            payload.fieldNames().forEachRemaining(actual::add);
            assertEquals(declared, actual);
            for (JsonNode field : schema.path("required")) {
                assertTrue(payload.hasNonNull(field.asText()), field.asText());
            }
            for (String field : declared) {
                JsonNode value = payload.get(field);
                JsonNode rule = schema.path("properties").path(field);
                String type =
                        value.isNull()
                                ? "null"
                                : value.isIntegralNumber()
                                        ? "integer"
                                        : value.isNumber()
                                                ? "number"
                                                : value.isTextual() ? "string" : "unsupported";
                JsonNode allowed = rule.path("type");
                boolean valid = allowed.isTextual() ? allowed.asText().equals(type) : false;
                for (JsonNode candidate : allowed) {
                    valid |=
                            candidate.asText().equals(type)
                                    || (type.equals("integer")
                                            && candidate.asText().equals("number"));
                }
                assertTrue(valid, field + " has unexpected wire type " + type);
                if (value.isTextual() && rule.has("minLength")) {
                    assertTrue(value.asText().length() >= rule.path("minLength").asInt(), field);
                }
            }
            assertEquals(
                    mapper.treeToValue(fixture, UserVerificationVerifyEvent.class),
                    mapper.treeToValue(payload, UserVerificationVerifyEvent.class));
        }
    }
}
