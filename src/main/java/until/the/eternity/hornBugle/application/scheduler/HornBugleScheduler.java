package until.the.eternity.hornBugle.application.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import until.the.eternity.hornBugle.application.service.HornBugleService;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.infrastructure.client.HornBugleClient;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryListResponse;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;
import until.the.eternity.hornBugle.kafka.application.HornBugleKafkaProducerService;
import until.the.eternity.hornBugle.kafka.dto.UserVerificationVerifyEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class HornBugleScheduler {

    private final HornBugleClient client;
    private final HornBugleService service;
    private final HornBugleKafkaProducerService hornBugleKafkaProducerService;

    private static final long RATE_LIMIT_DELAY_MS = 1000L;
    private static final Pattern CERTIFICATE_PATTERN = Pattern.compile("메모노기_([A-Z0-9]{20})");

    @Value("${openapi.horn-bugle.max-retries:3}")
    private int maxRetries;

    @Value("${openapi.horn-bugle.retry-delay-ms:2000}")
    private long retryDelayMs;

    @Scheduled(cron = "${openapi.horn-bugle.cron:0 */5 * * * *}", zone = "Asia/Seoul")
    public void fetchAndSaveHornBugleHistoryAll() {
        log.info("[HornBugle] Starting Horn Bugle World History scheduler");

        HornBugleServer[] servers = HornBugleServer.values();
        int totalSavedCount = 0;
        List<HornBugleServer> failedServers = new ArrayList<>();

        for (int i = 0; i < servers.length; i++) {
            HornBugleServer server = servers[i];
            int savedCount = fetchAndSaveForServer(server);

            if (savedCount < 0) {
                failedServers.add(server);
            } else {
                totalSavedCount += savedCount;
            }

            // Rate Limit: 마지막 서버가 아니면 1초 대기
            if (i < servers.length - 1) {
                waitForRateLimit();
            }
        }

        // 실패한 서버들 재시도
        if (!failedServers.isEmpty()) {
            log.info(
                    "[HornBugle] Retrying {} failed servers: {}",
                    failedServers.size(),
                    failedServers);
            totalSavedCount += retryFailedServers(failedServers);
        }

        log.info(
                "[HornBugle] Horn Bugle World History scheduler completed. Total saved: {}",
                totalSavedCount);
    }

    /**
     * 서버별 API 호출 및 저장
     *
     * @param server 서버
     * @return 저장된 건수 (-1: 실패)
     */
    private int fetchAndSaveForServer(HornBugleServer server) {
        try {
            OpenApiHornBugleHistoryListResponse response =
                    client.fetchHornBugleHistory(server).block();

            if (response == null || response.hornBugleWorldHistory() == null) {
                log.warn("[HornBugle] [{}] Empty response from API", server.getServerName());
                return 0;
            }

            List<OpenApiHornBugleHistoryResponse> histories = response.hornBugleWorldHistory();
            List<HornBugleWorldHistory> savedHistories =
                    service.saveAllAndReturnSaved(server, histories);
            int savedCount = savedHistories.size();

            publishVerificationEvents(savedHistories);

            log.info(
                    "[HornBugle] [{}] Fetched {} records, saved {} new records",
                    server.getServerName(),
                    histories.size(),
                    savedCount);

            return savedCount;
        } catch (Exception e) {
            log.error(
                    "[HornBugle] [{}] Failed to fetch and save: {}",
                    server.getServerName(),
                    e.getMessage(),
                    e);
            return -1;
        }
    }

    private void publishVerificationEvents(List<HornBugleWorldHistory> savedHistories) {
        for (HornBugleWorldHistory history : savedHistories) {
            Matcher matcher = CERTIFICATE_PATTERN.matcher(history.getMessage());

            while (matcher.find()) {
                String verificationValue = matcher.group(1);

                UserVerificationVerifyEvent event =
                        new UserVerificationVerifyEvent(
                                history.getCharacterName(),
                                history.getServerName(),
                                verificationValue,
                                history.getMessage(),
                                history.getDateSend());

                CompletableFuture<SendResult<String, Object>> publishFuture =
                        hornBugleKafkaProducerService.sendUserVerificationVerifyEvent(event);

                publishFuture.whenComplete(
                        (result, ex) -> {
                            if (ex == null && result != null) {
                                log.info(
                                        "[HornBugle] Verification event publish success. topic={}, partition={}, offset={}, payload={}",
                                        result.getRecordMetadata().topic(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset(),
                                        event);
                            } else {
                                log.error(
                                        "[HornBugle] Verification event publish failed. payload={}",
                                        event,
                                        ex);
                            }
                        });
            }
        }
    }

    /**
     * 실패한 서버들 재시도
     *
     * @param failedServers 실패한 서버 목록
     * @return 재시도로 저장된 총 건수
     */
    private int retryFailedServers(List<HornBugleServer> failedServers) {
        int totalSavedCount = 0;

        for (HornBugleServer server : failedServers) {
            int savedCount = retryForServer(server);
            if (savedCount >= 0) {
                totalSavedCount += savedCount;
            }
        }

        return totalSavedCount;
    }

    /**
     * 단일 서버 재시도 (지수 백오프)
     *
     * @param server 서버
     * @return 저장된 건수 (-1: 최종 실패)
     */
    private int retryForServer(HornBugleServer server) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                long delay = retryDelayMs * (long) Math.pow(2, attempt - 1);
                log.info(
                        "[HornBugle] [{}] Retry attempt {}/{}, waiting {}ms",
                        server.getServerName(),
                        attempt,
                        maxRetries,
                        delay);

                Thread.sleep(delay);

                int savedCount = fetchAndSaveForServer(server);
                if (savedCount >= 0) {
                    log.info(
                            "[HornBugle] [{}] Retry successful on attempt {}",
                            server.getServerName(),
                            attempt);
                    return savedCount;
                }
            } catch (InterruptedException e) {
                log.error("[HornBugle] [{}] Retry interrupted", server.getServerName(), e);
                Thread.currentThread().interrupt();
                return -1;
            }
        }

        log.error(
                "[HornBugle] [{}] All {} retry attempts failed",
                server.getServerName(),
                maxRetries);
        return -1;
    }

    private void waitForRateLimit() {
        try {
            log.debug("[HornBugle] Waiting {}ms for rate limit", RATE_LIMIT_DELAY_MS);
            Thread.sleep(RATE_LIMIT_DELAY_MS);
        } catch (InterruptedException e) {
            log.error("[HornBugle] Rate limit wait interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
