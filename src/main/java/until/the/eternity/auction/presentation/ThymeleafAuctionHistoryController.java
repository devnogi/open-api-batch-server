package until.the.eternity.auction.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import until.the.eternity.auction.domain.dto.AuctionHistorySearchCondition;
import until.the.eternity.auction.domain.model.AuctionHistory;
import until.the.eternity.auction.domain.service.AuctionHistoryService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/auction")
public class ThymeleafAuctionHistoryController {

    private final AuctionHistoryService auctionHistoryService;

    @GetMapping("/history")
    public String search(Model model) {

        // 검색 결과와 페이징된 결과 가져오기
        //        Page<AuctionHistory> auctionHistories = auctionHistoryService.search(condition,
        // pageable);
        //
        //        // 모델에 데이터를 추가
        //        model.addAttribute("auctionHistories", auctionHistories);
        //        model.addAttribute("condition", condition); // 검색 조건도 같이 전달

        AuctionHistory sample = auctionHistoryService.findByIdOrElseThrow(129996L);
        model.addAttribute("sample", sample);
        return "/pages/auction-history/list"; // Thymeleaf 템플릿의 경로
        // (src/main/resources/templates/auction-history/list.html)
    }

    @GetMapping("/history/index")
    public String getAuctionHistories(
            AuctionHistorySearchCondition condition, Pageable pageable, Model model) {
        Page<AuctionHistory> samples = auctionHistoryService.search(condition, pageable);
        model.addAttribute("samples", samples);
        return "/pages/auction-history/index";
    }
}
