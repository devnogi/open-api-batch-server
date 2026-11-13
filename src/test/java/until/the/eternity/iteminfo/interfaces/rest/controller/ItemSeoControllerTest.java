package until.the.eternity.iteminfo.interfaces.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.common.exception.GlobalExceptionHandler;
import until.the.eternity.iteminfo.application.service.ItemSeoService;
import until.the.eternity.iteminfo.domain.exception.ItemInfoExceptionCode;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSitemapRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSeoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSitemapResponse;

@ExtendWith(MockitoExtension.class)
class ItemSeoControllerTest {
    @Mock private ItemSeoService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new ItemSeoController(service))
                        .setMessageConverters(
                                new MappingJackson2HttpMessageConverter(
                                        Jackson2ObjectMapperBuilder.json()
                                                .featuresToDisable(
                                                        SerializationFeature
                                                                .WRITE_DATES_AS_TIMESTAMPS)
                                                .build()))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    void publicSeoResponseIncludesStoredIdentityAndNumericLastTrade() throws Exception {
        var item =
                ItemInfoResponse.builder()
                        .name("테스트 검")
                        .topCategory("장비")
                        .subCategory("검")
                        .description("합성 테스트 아이템")
                        .build();
        LocalDate date = LocalDate.of(2020, 1, 1);
        var lastTrade =
                new ItemSeoResponse.LastTrade(
                        "DAILY",
                        date,
                        date,
                        100L,
                        500L,
                        new BigDecimal("333.33"),
                        3L,
                        1000L,
                        LocalDateTime.of(2020, 1, 2, 0, 10));
        when(service.find(any())).thenReturn(new ItemSeoResponse(item, true, lastTrade));
        mockMvc.perform(
                        get("/api/item-infos/seo")
                                .param("name", "테스트 검")
                                .param("topCategory", "장비")
                                .param("subCategory", "검"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.item.name").value("테스트 검"))
                .andExpect(jsonPath("$.data.seoEligible").value(true))
                .andExpect(jsonPath("$.data.lastTrade.avgPrice").value(333.33))
                .andExpect(jsonPath("$.data.lastTrade.periodStart").value("2020-01-01"))
                .andExpect(jsonPath("$.data.lastTrade.updatedAt").value("2020-01-02T00:10:00"))
                .andExpect(
                        content()
                                .json(
                                        "{\"data\":"
                                                + Files.readString(
                                                        Path.of(
                                                                "contracts/item-seo-response.example.json"))
                                                + "}"));
    }

    @Test
    void sitemapDefaultLimitAndEmptyResultAreExplicit() throws Exception {
        when(service.sitemap(new ItemSitemapRequest(null, 1000)))
                .thenReturn(new ItemSitemapResponse(List.of(), null, false));
        mockMvc.perform(get("/api/item-infos/sitemap"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    void sitemapProjectionHasOnlyIdentityFields() throws Exception {
        when(service.sitemap(any()))
                .thenReturn(
                        new ItemSitemapResponse(
                                List.of(new ItemInfoSummaryResponse("검", "한손", "장비")),
                                "next",
                                true));
        mockMvc.perform(get("/api/item-infos/sitemap").param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("검"))
                .andExpect(jsonPath("$.data.items[0].description").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].updatedAt").doesNotExist())
                .andExpect(jsonPath("$.data.nextCursor").value("next"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "1001", "invalid"})
    void invalidLimitsReturn400WithoutServiceCalls(String limit) throws Exception {
        mockMvc.perform(get("/api/item-infos/sitemap").param("limit", limit))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void missingBlankAndOversizedIdentityReturns400() throws Exception {
        mockMvc.perform(get("/api/item-infos/seo")).andExpect(status().isBadRequest());
        mockMvc.perform(
                        get("/api/item-infos/seo")
                                .param("name", " ")
                                .param("topCategory", "장비")
                                .param("subCategory", "검"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(
                        get("/api/item-infos/seo")
                                .param("name", "검")
                                .param("topCategory", "a".repeat(26))
                                .param("subCategory", "검"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void missingCatalogAndDatabaseOutageAreDifferentStatuses() throws Exception {
        when(service.find(any()))
                .thenThrow(new CustomException(ItemInfoExceptionCode.ITEM_INFO_NOT_FOUND));
        mockMvc.perform(
                        get("/api/item-infos/seo")
                                .param("name", "검")
                                .param("topCategory", "장비")
                                .param("subCategory", "한손"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ITEM_INFO_NOT_FOUND"));
        when(service.sitemap(any())).thenThrow(new IllegalStateException("unavailable"));
        mockMvc.perform(get("/api/item-infos/sitemap"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void malformedCursorIs400NotAnEmptySitemap() throws Exception {
        when(service.sitemap(any()))
                .thenThrow(new CustomException(ItemInfoExceptionCode.INVALID_SITEMAP_CURSOR));
        mockMvc.perform(get("/api/item-infos/sitemap").param("cursor", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_SITEMAP_CURSOR"));
    }
}
