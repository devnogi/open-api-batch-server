package until.the.eternity.itemoptioninfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.itemoptioninfo.application.service.ItemOptionInfoService;
import until.the.eternity.itemoptioninfo.domain.mapper.ItemOptionInfoMapper;
import until.the.eternity.itemoptioninfo.interfaces.rest.dto.response.ItemOptionInfoResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/item-option-infos")
@RequiredArgsConstructor
@Tag(name = "아이템 옵션 정보", description = "아이템 옵션 정보 API")
public class ItemOptionInfoController {

    private final ItemOptionInfoService itemOptionInfoService;
    private final ItemOptionInfoMapper itemOptionInfoMapper;

    @GetMapping
    @Operation(summary = "아이템 옵션 정보 전체 조회", description = "모든 아이템 옵션 정보를 조회합니다.")
    public List<ItemOptionInfoResponse> findAll() {
        return itemOptionInfoService.findAll().stream()
                .map(itemOptionInfoMapper::toItemOptionInfoResponse)
                .collect(Collectors.toList());
    }
}
