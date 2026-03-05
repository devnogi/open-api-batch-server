package until.the.eternity.hornBugle.application.service;

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
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleDocument;
import until.the.eternity.hornBugle.infrastructure.elasticsearch.HornBugleIndexService;
import until.the.eternity.hornBugle.interfaces.external.dto.OpenApiHornBugleHistoryResponse;
import until.the.eternity.hornBugle.interfaces.rest.dto.request.HornBuglePageRequestDto;
import until.the.eternity.hornBugle.interfaces.rest.dto.response.HornBugleHistoryResponse;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class HornBugleService {

    private final HornBugleRepositoryPort repository;
    private final HornBugleDuplicateChecker duplicateChecker;
    private final HornBugleMapper mapper;
    private final Optional<HornBugleIndexService> indexService;

    public HornBugleService(
            HornBugleRepositoryPort repository,
            HornBugleDuplicateChecker duplicateChecker,
            HornBugleMapper mapper,
            Optional<HornBugleIndexService> indexService) {
        this.repository = repository;
        this.duplicateChecker = duplicateChecker;
        this.mapper = mapper;
        this.indexService = indexService;
    }

    /**
     * API 응답 데이터를 중복 제거 후 저장한다.
     *
     * @param server 서버 정보
     * @param responses API 응답 데이터
     * @return 저장된 건수
     */
    @Transactional
    public int saveAll(HornBugleServer server, List<OpenApiHornBugleHistoryResponse> responses) {
        return saveAllAndReturnSaved(server, responses).size();
    }

    @Transactional
    public List<HornBugleWorldHistory> saveAllAndReturnSaved(
            HornBugleServer server, List<OpenApiHornBugleHistoryResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            log.debug("[HornBugle] [{}] No data to save.", server.getServerName());
            return List.of();
        }

        // 중복 제거
        List<OpenApiHornBugleHistoryResponse> filtered =
                duplicateChecker.filterDuplicates(server, responses);

        if (filtered.isEmpty()) {
            log.debug(
                    "[HornBugle] [{}] All data is duplicated. Nothing to save.",
                    server.getServerName());
            return List.of();
        }

        // Entity 변환 및 저장
        Instant registerTime = Instant.now();
        List<HornBugleWorldHistory> entities =
                filtered.stream().map(dto -> mapper.toEntity(dto, server, registerTime)).toList();

        repository.saveAll(entities);

        // Elasticsearch 실시간 색인 (DB 저장 성공 후, ES가 활성화된 경우에만)
        indexService.ifPresent(service -> service.indexAll(entities));

        log.info("[HornBugle] [{}] Saved {} new records.", server.getServerName(), entities.size());

        return entities;
    }

    /**
     * 서버별 최신 N건 조회 (페이징). keyword가 있고 ES가 활성화되어 있으면 Elasticsearch 검색을 수행한다. ES가 비활성화되어 있거나 사용 불가능한
     * 경우 MySQL FULLTEXT 검색으로 fallback한다.
     *
     * @param serverName 서버 이름 (선택 사항, null이면 전체 조회)
     * @param keyword 검색 키워드 (선택 사항, null이면 DB 검색)
     * @param pageRequest 페이지 요청 정보
     * @return 페이징 응답
     */
    @Transactional(readOnly = true)
    public PageResponseDto<HornBugleHistoryResponse> search(
            String serverName, String keyword, HornBuglePageRequestDto pageRequest) {

        // keyword가 없으면 단순 DB 조회
        if (keyword == null || keyword.isBlank()) {
            return searchByDatabase(serverName, pageRequest);
        }

        // keyword가 있고 ES가 활성화되어 있고 사용 가능하면 Elasticsearch 검색
        if (indexService.isPresent() && indexService.get().isAvailable()) {
            try {
                return searchByElasticsearch(serverName, keyword, pageRequest);
            } catch (Exception e) {
                log.warn(
                        "[HornBugle] Elasticsearch search failed, falling back to MySQL FULLTEXT search. keyword={}, error={}",
                        keyword,
                        e.getMessage());
                return searchByDatabaseWithKeyword(serverName, keyword, pageRequest);
            }
        }

        // ES가 비활성화되어 있거나 사용 불가능한 경우 MySQL FULLTEXT 검색
        if (indexService.isEmpty()) {
            log.info(
                    "[HornBugle] Elasticsearch is not enabled. Using MySQL FULLTEXT search. keyword={}",
                    keyword);
        } else {
            log.warn(
                    "[HornBugle] Elasticsearch is not available. Falling back to MySQL FULLTEXT search. keyword={}",
                    keyword);
        }

        return searchByDatabaseWithKeyword(serverName, keyword, pageRequest);
    }

    /** Elasticsearch로 검색한다. */
    private PageResponseDto<HornBugleHistoryResponse> searchByElasticsearch(
            String serverName, String keyword, HornBuglePageRequestDto pageRequest) {

        Page<HornBugleDocument> page =
                indexService.get().search(keyword, serverName, pageRequest.toPageable());

        Page<HornBugleHistoryResponse> responsePage = page.map(mapper::toResponse);

        return PageResponseDto.of(responsePage);
    }

    /** DB로 검색한다 (keyword 없이). */
    private PageResponseDto<HornBugleHistoryResponse> searchByDatabase(
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

    /**
     * MySQL FULLTEXT 검색으로 keyword 검색을 수행한다. Native Query에서 ORDER BY를 지정하므로 Sort 없이 Pageable을 전달한다.
     */
    private PageResponseDto<HornBugleHistoryResponse> searchByDatabaseWithKeyword(
            String serverName, String keyword, HornBuglePageRequestDto pageRequest) {

        Page<HornBugleWorldHistory> page;

        if (serverName != null && !serverName.isBlank()) {
            page =
                    repository.searchByKeywordAndServerName(
                            keyword, serverName, pageRequest.toPageableWithoutSort());
        } else {
            page = repository.searchByKeyword(keyword, pageRequest.toPageableWithoutSort());
        }

        Page<HornBugleHistoryResponse> responsePage = page.map(mapper::toResponse);

        return PageResponseDto.of(responsePage);
    }
}
