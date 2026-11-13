package until.the.eternity.iteminfo.interfaces.rest.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import until.the.eternity.common.response.ApiResponse;
import until.the.eternity.iteminfo.application.service.ItemSeoService;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSeoRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemSitemapRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSeoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemSitemapResponse;

@RestController
@RequestMapping("/api/item-infos")
@RequiredArgsConstructor
public class ItemSeoController {
    private final ItemSeoService service;

    @GetMapping("/seo")
    public ResponseEntity<ApiResponse<ItemSeoResponse>> seo(
            @Valid @ModelAttribute ItemSeoRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(service.find(request)));
    }

    @GetMapping("/sitemap")
    public ResponseEntity<ApiResponse<ItemSitemapResponse>> sitemap(
            @Valid @ModelAttribute ItemSitemapRequest request) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ApiResponse.success(service.sitemap(request)));
    }
}
