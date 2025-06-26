package until.the.eternity.auction.domain.dto.internal.response;

public record ItemOptionResponse(
        Long id,
        String optionType,
        String optionSubType,
        String optionValue,
        String optionValue2,
        String optionDesc) {}
