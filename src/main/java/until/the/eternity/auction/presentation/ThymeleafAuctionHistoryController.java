package until.the.eternity.auction.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

        AuctionHistory sample = auctionHistoryService.findByIdOrElseThrow(129996L);
        model.addAttribute("sample", sample);
        return "/pages/auction-history/list";
    }

    @GetMapping("/history/index")
    public String getAuctionHistories(
            @ModelAttribute AuctionHistorySearchCondition condition,
            Pageable pageable,
            Model model) {
        Page<AuctionHistory> samples = auctionHistoryService.search(condition, pageable);
        model.addAttribute("samples", samples);
        return "/pages/auction-history/index";
    }
}
