package until.the.eternity.enchantinfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.enchantinfo.application.service.EnchantInfoService;
import until.the.eternity.enchantinfo.interfaces.rest.dto.request.EnchantInfoPageRequestDto;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoResponse;
import until.the.eternity.enchantinfo.interfaces.rest.dto.response.EnchantInfoSyncResponse;

@RestController
@RequestMapping("/api/enchant-infos")
@RequiredArgsConstructor
@Tag(name = "Enchant Info", description = "인챈트 정보 API")
public class EnchantInfoController {

    private final EnchantInfoService enchantInfoService;

    @Operation(
            summary = "인챈트 정보 페이지네이션 조회",
            description =
                    "인챈트 정보를 페이지네이션과 함께 조회합니다. "
                            + "정렬 기준은 id(ASC/DESC)이며 기본값은 ASC입니다. "
                            + "page는 1 이상, size는 10~50 사이 값만 허용됩니다.")
    @GetMapping
    public ResponseEntity<Page<EnchantInfoResponse>> getEnchantInfos(
            @Valid @ModelAttribute EnchantInfoPageRequestDto pageRequest) {
        return ResponseEntity.ok(enchantInfoService.findAll(pageRequest.toPageable()));
    }

    @Operation(
            summary = "모든 인챈트 fullname 조회",
            description =
                    "페이지네이션 없이 저장된 모든 인챈트의 fullname(이름 및 랭크)을 한 번에 조회합니다. "
                            + "affix_position을 지정하면 해당 위치(접두/접미)의 인챈트만 필터링합니다.")
    @ApiResponse(responseCode = "400", description = "잘못된 affix_position 값 (접두 또는 접미만 허용)")
    @GetMapping("/fullnames")
    public List<String> getAllEnchantFullnames(
            @Parameter(description = "접두/접미 구분 필터 (허용값: 접두, 접미)", example = "접두")
                    @RequestParam(name = "affix_position", required = false)
                    String affixPosition) {
        return enchantInfoService.findAllFullnames(affixPosition);
    }

    @Operation(
            summary = "인챈트 정보 동기화",
            description =
                    "auction_history_item_option을 기반으로 enchant_info를 업서트합니다. "
                            + "**[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping("/sync")
    public ResponseEntity<EnchantInfoSyncResponse> syncEnchantInfo() {
        return ResponseEntity.ok(enchantInfoService.syncFromAuctionHistory());
    }
}
