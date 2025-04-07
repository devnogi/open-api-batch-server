package until.the.eternity.auction.domain.component;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import until.the.eternity.auction.domain.dto.AuctionHistoryDto;
import until.the.eternity.auction.domain.dto.AuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

@Component
@RequiredArgsConstructor
public class AuctionHistoryFetcher {

    private final WebClient webClient;

    @Value("${openapi.nexon.api-key}")
    private String nexonApiKey;

    public List<AuctionHistoryDto> fetch(ItemCategory category) {
        AuctionHistoryResponse response =
                webClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/auction/history")
                                                .queryParam(
                                                        "auction_item_category",
                                                        category.getItemName())
                                                .build())
                        .header("x-nxopen-api-key", nexonApiKey)
                        .header("accept", "application/json")
                        .retrieve()
                        .bodyToMono(AuctionHistoryResponse.class)
                        .block();

        return response != null ? response.getAuction_history() : null;
    }
}
