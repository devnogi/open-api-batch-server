package until.the.eternity.itemoptioninfo.domain.mapper;

import org.springframework.stereotype.Component;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfoId;
import until.the.eternity.itemoptioninfo.interfaces.rest.dto.request.ItemOptionInfoRequest;
import until.the.eternity.itemoptioninfo.interfaces.rest.dto.response.ItemOptionInfoResponse;

@Component
public class ItemOptionInfoMapper {

    public ItemOptionInfoResponse toItemOptionInfoResponse(ItemOptionInfo itemOptionInfo) {
        return ItemOptionInfoResponse.builder()
                .optionType(itemOptionInfo.getId().getOptionType())
                .optionSubType(itemOptionInfo.getId().getOptionSubType())
                .optionValue(itemOptionInfo.getId().getOptionValue())
                .optionValue2(itemOptionInfo.getId().getOptionValue2())
                .optionDesc(itemOptionInfo.getOptionDesc())
                .build();
    }

    public ItemOptionInfo toEntity(ItemOptionInfoRequest request) {
        ItemOptionInfoId id =
                new ItemOptionInfoId(
                        request.getOptionType(),
                        request.getOptionSubType(),
                        request.getOptionValue(),
                        request.getOptionValue2());
        return new ItemOptionInfo(id, request.getOptionDesc());
    }

    public ItemOptionInfoId toId(ItemOptionInfoRequest request) {
        return new ItemOptionInfoId(
                request.getOptionType(),
                request.getOptionSubType(),
                request.getOptionValue(),
                request.getOptionValue2());
    }
}
