package until.the.eternity.auctionsearchoption.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.auctionsearchoption.application.service.AuctionSearchOptionService;
import until.the.eternity.auctionsearchoption.interfaces.rest.dto.response.SearchOptionMetadataResponse;
import until.the.eternity.common.response.ApiResponse;

@Tag(name = "Auction Search Option", description = "경매 검색 옵션 API")
@RestController
@RequestMapping("/api/search-option")
@RequiredArgsConstructor
public class AuctionSearchOptionController {

    private final AuctionSearchOptionService service;

    @Operation(summary = "검색 옵션 메타데이터 조회", description = "경매 검색에 사용 가능한 모든 옵션 메타데이터를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SearchOptionMetadataResponse>>> getSearchOptions() {
        List<SearchOptionMetadataResponse> searchOptions = service.getAllActiveSearchOptions();

        return ResponseEntity.ok(
                ApiResponse.success("SEARCH_OPTION_SUCCESS", "검색 옵션 조회 성공", searchOptions));
    }
}
