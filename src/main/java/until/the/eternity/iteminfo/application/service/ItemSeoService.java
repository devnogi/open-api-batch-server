package until.the.eternity.iteminfo.application.service;

import static until.the.eternity.iteminfo.domain.exception.ItemInfoExceptionCode.INVALID_SITEMAP_CURSOR;
import static until.the.eternity.iteminfo.domain.exception.ItemInfoExceptionCode.ITEM_INFO_NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.domain.repository.ItemSeoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSeoRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSitemapRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSeoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSeoResponse.LastTrade;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSitemapResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemSeoService {
    private final ItemSeoRepositoryPort repository;
    private final ObjectMapper objectMapper;

    public ItemSeoResponse find(ItemSeoRequest request) {
        ItemInfoId id =
                new ItemInfoId(request.name(), request.subCategory(), request.topCategory());
        var entry =
                repository
                        .findSeoItem(id)
                        .orElseThrow(() -> new CustomException(ITEM_INFO_NOT_FOUND));
        // Use stored identity for statistics and the consumer's canonical URL.
        ItemInfoId storedId = entry.item().getId();
        LastTrade daily =
                repository
                        .findLastDaily(storedId)
                        .map(
                                value ->
                                        new LastTrade(
                                                "DAILY",
                                                value.getDateAuctionBuy(),
                                                value.getDateAuctionBuy(),
                                                value.getMinPrice(),
                                                value.getMaxPrice(),
                                                average(
                                                        value.getTotalVolume(),
                                                        value.getTotalQuantity(),
                                                        value.getAvgPrice()),
                                                value.getTotalQuantity(),
                                                value.getTotalVolume(),
                                                value.getUpdatedAt()))
                        .orElse(null);
        LastTrade weekly =
                repository
                        .findLastWeekly(storedId)
                        .map(
                                value ->
                                        new LastTrade(
                                                "WEEKLY",
                                                value.getWeekStartDate(),
                                                value.getWeekStartDate().plusDays(6),
                                                value.getMinPrice(),
                                                value.getMaxPrice(),
                                                average(
                                                        value.getTotalVolume(),
                                                        value.getTotalQuantity(),
                                                        value.getAvgPrice()),
                                                value.getTotalQuantity(),
                                                value.getTotalVolume(),
                                                value.getUpdatedAt()))
                        .orElse(null);
        LastTrade lastTrade = daily;
        if (weekly != null
                && (daily == null || weekly.periodStart().isAfter(daily.periodStart()))) {
            lastTrade = weekly;
        }
        return new ItemSeoResponse(
                ItemInfoResponse.from(entry.item()), entry.eligible(), lastTrade);
    }

    public ItemSitemapResponse sitemap(ItemSitemapRequest request) {
        ItemInfoId cursor = decodeCursor(request.cursor());
        List<ItemInfoSummaryResponse> rows =
                repository.findSitemapItems(cursor, request.limit() + 1);
        boolean hasNext = rows.size() > request.limit();
        List<ItemInfoSummaryResponse> items =
                List.copyOf(rows.subList(0, Math.min(rows.size(), request.limit())));
        String nextCursor = hasNext ? encodeCursor(items.getLast()) : null;
        return new ItemSitemapResponse(items, nextCursor, hasNext);
    }

    private BigDecimal average(Long amount, Long quantity, BigDecimal storedAverage) {
        return quantity > 0
                ? BigDecimal.valueOf(amount)
                        .divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP)
                : storedAverage;
    }

    private ItemInfoId decodeCursor(String cursor) {
        if (cursor == null) return null;
        if (cursor.isEmpty() || cursor.length() > 8192 || !cursor.matches("[A-Za-z0-9_-]+")) {
            throw new CustomException(INVALID_SITEMAP_CURSOR);
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cursor);
            String json =
                    StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
            JsonNode values =
                    objectMapper
                            .reader()
                            .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                            .readTree(json);
            if (values == null || !values.isArray() || values.size() != 3) {
                throw new CustomException(INVALID_SITEMAP_CURSOR);
            }
            for (int index = 0; index < values.size(); index++) {
                JsonNode value = values.get(index);
                int maximumLength = index == 0 ? 255 : 25;
                if (!value.isTextual()
                        || value.asText().isBlank()
                        || value.asText().length() > maximumLength) {
                    throw new CustomException(INVALID_SITEMAP_CURSOR);
                }
            }
            return new ItemInfoId(
                    values.get(0).asText(), values.get(2).asText(), values.get(1).asText());
        } catch (IllegalArgumentException
                | JsonProcessingException
                | CharacterCodingException exception) {
            throw new CustomException(INVALID_SITEMAP_CURSOR);
        }
    }

    private String encodeCursor(ItemInfoSummaryResponse item) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(
                            objectMapper.writeValueAsBytes(
                                    List.of(item.name(), item.topCategory(), item.subCategory())));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize the sitemap cursor", exception);
        }
    }
}
