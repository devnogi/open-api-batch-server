package until.the.eternity.auctionhistory.interfaces.rest.dto.response;

public record ItemOptionResponse(
        Long id,
        String optionType,
        String optionSubType,
        String optionValue,
        String optionValue2,
        String optionDesc) {}
