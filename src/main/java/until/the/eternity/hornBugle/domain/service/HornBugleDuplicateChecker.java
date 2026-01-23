package until.the.eternity.hornBugle.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HornBugleDuplicateChecker {

    private final HornBugleRepositoryPort repository;

    /**
     * API 응답 데이터에서 중복을 제거하고 신규 데이터만 반환한다.
     *
     * <p>중복 제거 로직: 1. DB에서 해당 서버의 가장 최근 date_send를 조회 2. API 응답 데이터 중 DB의 최신 date_send 이전인 데이터는 모두
     * 제거 3. date_send가 DB의 최신 date_send와 동일한 경우: server_name + character_name + message 기준으로 중복 검증
     * 4. DB 최신 date_send 이후의 데이터는 모두 신규 데이터로 간주
     *
     * @param server 서버 정보
     * @param responses API 응답 데이터 (date_send desc 정렬)
     * @return 중복이 제거된 신규 데이터 목록
     */
    public List<OpenApiHornBugleHistoryResponse> filterDuplicates(
            HornBugleServer server, List<OpenApiHornBugleHistoryResponse> responses) {

        if (responses == null || responses.isEmpty()) {
            return List.of();
        }

        String serverName = server.getServerName();
        Optional<HornBugleWorldHistory> latestRecordOpt =
                repository.findLatestByServerName(serverName);

        if (latestRecordOpt.isEmpty()) {
            log.debug(
                    "[HornBugle] [{}] No existing data found. All {} responses are new.",
                    serverName,
                    responses.size());
            return responses;
        }

        Instant latestDateSend = latestRecordOpt.get().getDateSend();
        log.debug("[HornBugle] [{}] Latest date_send in DB: {}", serverName, latestDateSend);

        // 동일한 date_send를 가진 기존 데이터들의 (character_name + message) 조합을 조회
        Set<String> existingKeys = buildExistingKeysForDateSend(serverName, latestDateSend);

        List<OpenApiHornBugleHistoryResponse> filtered =
                responses.stream()
                        .filter(
                                response -> {
                                    Instant responseDateSend = response.dateSend();

                                    // 최신 date_send보다 이전인 데이터는 제거
                                    if (responseDateSend.isBefore(latestDateSend)) {
                                        return false;
                                    }

                                    // date_send가 동일한 경우: 중복 키 체크
                                    if (responseDateSend.equals(latestDateSend)) {
                                        String key =
                                                buildDuplicateKey(
                                                        response.characterName(),
                                                        response.message());
                                        return !existingKeys.contains(key);
                                    }

                                    // 최신 date_send보다 이후인 데이터는 신규
                                    return true;
                                })
                        .toList();

        log.info(
                "[HornBugle] [{}] Filtered {} duplicates. {} new records to save.",
                serverName,
                responses.size() - filtered.size(),
                filtered.size());

        return filtered;
    }

    private Set<String> buildExistingKeysForDateSend(String serverName, Instant dateSend) {
        List<HornBugleWorldHistory> existingRecords =
                repository.findByServerNameAndDateSend(serverName, dateSend);

        Set<String> keys = new HashSet<>();
        for (HornBugleWorldHistory record : existingRecords) {
            keys.add(buildDuplicateKey(record.getCharacterName(), record.getMessage()));
        }

        log.debug(
                "[HornBugle] [{}] Found {} existing records with date_send={}",
                serverName,
                keys.size(),
                dateSend);

        return keys;
    }

    private String buildDuplicateKey(String characterName, String message) {
        return characterName + "|" + message;
    }
}
