package until.the.eternity.auctionrealtime.interfaces.rest.dto.response;

public record RealtimeItemOptionResponse(
        String id,
        String optionType,
        String optionSubType,
        String optionValue,
        String optionValue2,
        String optionDesc) {}
