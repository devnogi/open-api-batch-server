package until.the.eternity.itemoptioninfo.domain.mapper;

import org.springframework.stereotype.Component;
import until.the.eternity.itemoptioninfo.domain.entity.ItemOptionInfo;
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
}
