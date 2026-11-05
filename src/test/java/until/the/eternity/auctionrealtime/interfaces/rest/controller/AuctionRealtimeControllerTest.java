package until.the.eternity.auctionrealtime.interfaces.rest.controller;

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
import until.the.eternity.auctionrealtime.application.service.AuctionRealtimeService;
import until.the.eternity.auctionrealtime.domain.exception.AuctionRealtimeExceptionCode;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.AuctionRealtimeDetailResponse;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.response.RealtimeItemOptionResponse;
import until.the.eternity.common.exception.CustomException;
import until.the.eternity.common.exception.GlobalExceptionHandler;

@ExtendWith(MockitoExtension.class)
class AuctionRealtimeControllerTest {

    @Mock private AuctionRealtimeService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc =
                MockMvcBuilders.standaloneSetup(new AuctionRealtimeController(service))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
    }

    @Test
    @DisplayName("단건 조회는 카테고리, 등록·만료 시각, 옵션을 포함한다")
    void findById_should_return_complete_detail_contract() throws Exception {
        Long id = 10L;
        AuctionRealtimeDetailResponse<RealtimeItemOptionResponse> response =
                new AuctionRealtimeDetailResponse<>(
                        id,
                        "켈틱 로열 나이트 소드",
                        "정교한 켈틱 로열 나이트 소드",
                        1L,
                        1_400_000L,
                        Instant.parse("2026-11-05T06:00:00Z"),
                        Instant.parse("2026-11-05T03:00:00Z"),
                        "검",
                        "근거리 장비",
                        List.of(
                                new RealtimeItemOptionResponse(
                                        "option-1", "인챈트", "접두", "정교한", null, "최대 대미지 증가")));
        when(service.findByIdOrElseThrow(id)).thenReturn(response);

        mockMvc.perform(get("/auction-realtime/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.itemName").value("켈틱 로열 나이트 소드"))
                .andExpect(jsonPath("$.itemSubCategory").value("검"))
                .andExpect(jsonPath("$.itemTopCategory").value("근거리 장비"))
                .andExpect(jsonPath("$.dateRegister").value("2026-11-05T03:00:00Z"))
                .andExpect(jsonPath("$.dateAuctionExpire").value("2026-11-05T06:00:00Z"))
                .andExpect(jsonPath("$.itemOptions[0].optionType").value("인챈트"))
                .andExpect(jsonPath("$.itemOptions[0].optionValue").value("정교한"));
    }

    @Test
    @DisplayName("존재하지 않는 실시간 매물은 404 계약으로 응답한다")
    void findById_should_return_not_found_contract() throws Exception {
        Long id = 999L;
        when(service.findByIdOrElseThrow(id))
                .thenThrow(
                        new CustomException(
                                AuctionRealtimeExceptionCode.AUCTION_REALTIME_ITEM_NOT_FOUND));

        mockMvc.perform(get("/auction-realtime/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUCTION_REALTIME_ITEM_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("실시간 경매장 아이템을 찾을 수 없습니다."));
    }
}
