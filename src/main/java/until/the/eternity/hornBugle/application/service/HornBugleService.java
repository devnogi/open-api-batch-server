package until.the.eternity.hornBugle.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.hornBugle.domain.entity.HornBugleWorldHistory;
import until.the.eternity.hornBugle.domain.enums.HornBugleServer;
import until.the.eternity.hornBugle.domain.mapper.HornBugleMapper;
import until.the.eternity.hornBugle.domain.repository.HornBugleRepositoryPort;
import until.the.eternity.hornBugle.domain.service.HornBugleDuplicateChecker;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HornBugleService {

    private final HornBugleRepositoryPort repository;
    private final HornBugleDuplicateChecker duplicateChecker;
    private final HornBugleMapper mapper;

    /**
     * API 응답 데이터를 중복 제거 후 저장한다.
     *
     * @param server 서버 정보
     * @param responses API 응답 데이터
     * @return 저장된 건수
     */
    @Transactional
    public int saveAll(HornBugleServer server, List<OpenApiHornBugleHistoryResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            log.debug("[HornBugle] [{}] No data to save.", server.getServerName());
            return 0;
        }

        // 중복 제거
        List<OpenApiHornBugleHistoryResponse> filtered =
                duplicateChecker.filterDuplicates(server, responses);

        if (filtered.isEmpty()) {
            log.debug(
                    "[HornBugle] [{}] All data is duplicated. Nothing to save.",
                    server.getServerName());
            return 0;
        }

        // Entity 변환 및 저장
        Instant registerTime = Instant.now();
        List<HornBugleWorldHistory> entities =
                filtered.stream().map(dto -> mapper.toEntity(dto, server, registerTime)).toList();

        repository.saveAll(entities);

        log.info("[HornBugle] [{}] Saved {} new records.", server.getServerName(), entities.size());

        return entities.size();
    }

    /**
     * 서버별 최신 N건 조회 (페이징)
     *
     * @param serverName 서버 이름 (선택 사항, null이면 전체 조회)
     * @param pageRequest 페이지 요청 정보
     * @return 페이징 응답
     */
    @Transactional(readOnly = true)
    public PageResponseDto<HornBugleHistoryResponse> search(
            String serverName, HornBuglePageRequestDto pageRequest) {

        Page<HornBugleWorldHistory> page;

        if (serverName != null && !serverName.isBlank()) {
            page = repository.findByServerName(serverName, pageRequest.toPageable());
        } else {
            page = repository.findAll(pageRequest.toPageable());
        }

        Page<HornBugleHistoryResponse> responsePage = page.map(mapper::toResponse);

        return PageResponseDto.of(responsePage);
    }
}
