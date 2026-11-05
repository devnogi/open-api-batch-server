package until.the.eternity.auctionhistory.interfaces.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import until.the.eternity.auctionhistory.application.scheduler.AuctionHistoryScheduler;
import until.the.eternity.auctionhistory.application.service.AuctionHistoryService;
import until.the.eternity.auctionhistory.domain.exception.AuctionHistoryExceptionCode;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.AuctionHistoryDetailResponse;
import until.the.eternity.auctionhistory.interfaces.rest.dto.response.ItemOptionResponse;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.common.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryControllerTest {

    @Mock private AuctionHistoryService service;
    @Mock private AuctionHistoryScheduler scheduler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new AuctionHistoryController(service, scheduler))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    @DisplayName("단건 조회는 카테고리, 거래 시각, 옵션을 포함한다")
    void findById_should_return_complete_detail_contract() throws Exception {
        String id = "buy-1";
        AuctionHistoryDetailResponse<ItemOptionResponse> response =
                new AuctionHistoryDetailResponse<>(
                        "켈틱 로열 나이트 소드",
                        "정교한 켈틱 로열 나이트 소드",
                        2L,
                        1_500_000L,
                        Instant.parse("2026-11-04T03:00:00Z"),
                        id,
                        "검",
                        "근거리 장비",
                        List.of(
                                new ItemOptionResponse(
                                        "option-1", "인챈트", "접두", "정교한", null, "최대 대미지 증가")));
        when(service.findByIdOrElseThrow(id)).thenReturn(response);

        mockMvc.perform(get("/auction-history/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionBuyId").value(id))
                .andExpect(jsonPath("$.itemName").value("켈틱 로열 나이트 소드"))
                .andExpect(jsonPath("$.itemSubCategory").value("검"))
                .andExpect(jsonPath("$.itemTopCategory").value("근거리 장비"))
                .andExpect(jsonPath("$.dateAuctionBuy").value("2026-11-04T03:00:00Z"))
                .andExpect(jsonPath("$.itemOptions[0].optionType").value("인챈트"))
                .andExpect(jsonPath("$.itemOptions[0].optionValue").value("정교한"));
    }

    @Test
    @DisplayName("존재하지 않는 거래 내역은 404 계약으로 응답한다")
    void findById_should_return_not_found_contract() throws Exception {
        String id = "missing";
        when(service.findByIdOrElseThrow(id))
                .thenThrow(
                        new CustomException(AuctionHistoryExceptionCode.AUCTION_HISTORY_NOT_FOUND));

        mockMvc.perform(get("/auction-history/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUCTION_HISTORY_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("경매장 거래 내역을 찾을 수 없습니다."));
    }
}
