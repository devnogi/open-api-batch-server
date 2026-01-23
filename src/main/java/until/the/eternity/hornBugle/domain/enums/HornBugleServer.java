package until.the.eternity.hornBugle.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum HornBugleServer {
    LUTE("류트"),
    MANDOLIN("만돌린"),
    HARP("하프"),
    WOLF("울프");

    private final String serverName;

    public String getEncodedServerName() {
        return URLEncoder.encode(serverName, StandardCharsets.UTF_8);
    }

    public static HornBugleServer fromServerName(String serverName) {
        return Arrays.stream(values())
                .filter(server -> server.serverName.equals(serverName))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Unknown server name: " + serverName));
    }
}
