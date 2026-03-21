package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.dto.BannerDTO;
import com.zhitu.platform.dto.SaveBannerRequest;
import com.zhitu.platform.dto.SaveTopListRequest;
import com.zhitu.platform.dto.TopListDTO;
import com.zhitu.platform.service.PlatformRecommendationService;
import com.zhitu.platform.service.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台门户接口（/api/portal-platform/v1）
 */
@RestController
@RequestMapping("/api/portal-platform/v1")
@RequiredArgsConstructor
public class PlatformPortalController {

    private final PlatformService platformService;
    private final PlatformRecommendationService recommendationService;

    // ── Recommendation Banners ────────────────────────────────────────────────

    /**
     * 获取推荐横幅列表
     * GET /api/portal-platform/v1/recommendations/banner
     * 
     * Requirements: 37.1, 37.4, 37.5, 37.6
     */
    @GetMapping("/recommendations/banner")
    public Result<List<BannerDTO>> getRecommendationBanners(
            @RequestParam(value = "portal", required = false) String portal) {
        return Result.ok(recommendationService.getBanners(portal));
    }

    /**
     * 保存推荐横幅
     * POST /api/portal-platform/v1/recommendations/banner
     * 
     * Requirements: 37.2, 37.3
     */
    @PostMapping("/recommendations/banner")
    public Result<Void> saveRecommendationBanner(@RequestBody SaveBannerRequest request) {
        recommendationService.saveBanner(request);
        return Result.ok();
    }

    // ── Top List ──────────────────────────────────────────────────────────────

    /**
     * 获取推荐榜单
     * GET /api/portal-platform/v1/recommendations/top-list
     * 
     * Requirements: 38.1, 38.3
     */
    @GetMapping("/recommendations/top-list")
    public Result<TopListDTO> getTopList(
            @RequestParam(value = "listType", required = true) String listType) {
        return Result.ok(recommendationService.getTopList(listType));
    }

    /**
     * 保存推荐榜单
     * POST /api/portal-platform/v1/recommendations/top-list
     * 
     * Requirements: 38.2, 38.4, 38.5, 38.6
     */
    @PostMapping("/recommendations/top-list")
    public Result<Void> saveTopList(@RequestBody SaveTopListRequest request) {
        recommendationService.saveTopList(request);
        return Result.ok();
    }
}
