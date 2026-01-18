package until.the.eternity.metalwareinfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.metalwareinfo.application.service.MetalwareInfoService;
import until.the.eternity.metalwareinfo.interfaces.rest.dto.response.MetalwareInfoResponse;

import java.util.List;

@RestController
@RequestMapping("/api/metalware-infos")
@RequiredArgsConstructor
@Tag(name = "Metalware Info", description = "세공 정보 조회 API")
public class MetalwareInfoController {

    private final MetalwareInfoService metalwareInfoService;

    // TODO: 세공 레벨별 능력치, 최대 레벨, 한계 돌파 최대 레벨은 수기로 추가 후 조회 시 사용
    @Operation(summary = "모든 세공 정보 조회", description = "시스템에 저장된 모든 세공 정보를 조회합니다.")
    @GetMapping
    public List<MetalwareInfoResponse> getAllMetalwareInfos() {
        return metalwareInfoService.findAll();
    }
}
