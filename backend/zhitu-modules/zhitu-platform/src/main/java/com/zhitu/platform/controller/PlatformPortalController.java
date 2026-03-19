package com.zhitu.platform.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.platform.service.PlatformService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 平台门户接口（/api/portal-platform/v1）
 */
@RestController
@RequestMapping("/api/portal-platform/v1")
@RequiredArgsConstructor
public class PlatformPortalController {

    private final PlatformService platformService;

    // ── Recommendation Banners ────────────────────────────────────────────────

    @GetMapping("/recommendations/banner")
    public Result<List<Map<String, Object>>> getRecommendationBanners() {
        return Result.ok(platformService.getRecommendationBanners());
    }

    @PostMapping("/recommendations/banner")
    public Result<Void> saveRecommendationBanner(@RequestBody Map<String, Object> req) {
        platformService.saveRecommendationBanner(req);
        return Result.ok();
    }

    // ── Top List ──────────────────────────────────────────────────────────────

    @GetMapping("/recommendations/top-list")
    public Result<List<Map<String, Object>>> getTopListItems(
            @RequestParam String listType) {
        return Result.ok(platformService.getTopListItems(listType));
    }

    @PostMapping("/recommendations/top-list")
    public Result<Void> saveTopListItems(@RequestBody Map<String, Object> req) {
        platformService.saveTopListItems(req);
        return Result.ok();
    }
}
