package until.the.eternity.itemoptioninfo.interfaces.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import until.the.eternity.itemoptioninfo.application.service.ItemOptionInfoService;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;
import until.the.eternity.itemoptioninfo.domain.mapper.ItemOptionInfoMapper;
import until.the.eternity.itemoptioninfo.interfaces.rest.dto.request.ItemOptionInfoRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "아이템 옵션 정보 생성", description = "새로운 아이템 옵션 정보를 생성합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ItemOptionInfoResponse create(@Valid @RequestBody ItemOptionInfoRequest request) {
        ItemOptionInfo itemOptionInfo = itemOptionInfoMapper.toEntity(request);
        ItemOptionInfo saved = itemOptionInfoService.create(itemOptionInfo);
        return itemOptionInfoMapper.toItemOptionInfoResponse(saved);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "아이템 옵션 정보 수정",
            description =
                    "아이템 옵션 정보를 수정합니다. 복합 키(optionType, optionSubType, optionValue, optionValue2)로 식별하며, optionDesc만 수정 가능합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public ItemOptionInfoResponse update(@Valid @RequestBody ItemOptionInfoRequest request) {
        ItemOptionInfoId id = itemOptionInfoMapper.toId(request);
        ItemOptionInfo updated = itemOptionInfoService.update(id, request.getOptionDesc());
        return itemOptionInfoMapper.toItemOptionInfoResponse(updated);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(
            summary = "아이템 옵션 정보 삭제",
            description =
                    "아이템 옵션 정보를 삭제합니다. 복합 키(optionType, optionSubType, optionValue, optionValue2)로 식별합니다. **[ADMIN, SUPER_ADMIN 전용]**")
    @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN, SUPER_ADMIN 전용)")
    public void delete(@Valid @RequestBody ItemOptionInfoRequest request) {
        ItemOptionInfoId id = itemOptionInfoMapper.toId(request);
        itemOptionInfoService.delete(id);
    }
}
