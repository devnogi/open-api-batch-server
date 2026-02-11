package until.the.eternity.metalwareinfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.common.response.PageResponseDto;
import until.the.eternity.metalwareinfo.application.service.MetalwareAttributeInfoService;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.request.MetalwareAttributeInfoSearchRequest;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareAttributeInfoResponse;

@RestController
@RequestMapping("/api/metalware-attribute-infos")
@RequiredArgsConstructor
@Tag(name = "Metalware Attribute Info", description = "세공 능력치 정보 조회 및 동기화 API")
public class MetalwareAttributeInfoController {

    private final MetalwareAttributeInfoService metalwareAttributeInfoService;

    @Operation(summary = "세공 능력치 정보 동기화", description = "경매 기록에서 세공 능력치 정보를 추출하여 동기화합니다.")
    @PostMapping("/sync")
    public ResponseEntity<Integer> sync() {
        int syncedCount = metalwareAttributeInfoService.sync();
        return ResponseEntity.ok(syncedCount);
    }

    @Operation(summary = "세공 능력치 정보 검색", description = "세공 이름으로 능력치 정보를 검색합니다.")
    @GetMapping
    public ResponseEntity<PageResponseDto<MetalwareAttributeInfoResponse>> search(
            @ParameterObject @ModelAttribute @Valid MetalwareAttributeInfoSearchRequest request) {
        return ResponseEntity.ok(PageResponseDto.of(metalwareAttributeInfoService.search(request)));
    }
}
