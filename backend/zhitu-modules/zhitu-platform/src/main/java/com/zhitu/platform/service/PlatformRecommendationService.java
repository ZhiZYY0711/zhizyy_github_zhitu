package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.common.redis.constants.CacheConstants;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.BannerDTO;
import com.zhitu.platform.dto.SaveBannerRequest;
import com.zhitu.platform.dto.SaveTopListRequest;
import com.zhitu.platform.dto.TopListDTO;
import com.zhitu.platform.entity.RecommendationBanner;
import com.zhitu.platform.entity.RecommendationTopList;
import com.zhitu.platform.mapper.RecommendationBannerMapper;
import com.zhitu.platform.mapper.RecommendationTopListMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 平台推荐服务
 * Platform Recommendation Service
 * 
 * Handles recommendation banners and top lists for platform administration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformRecommendationService {

    private final RecommendationBannerMapper bannerMapper;
    private final RecommendationTopListMapper topListMapper;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取推荐横幅列表
     * Get recommendation banners filtered by portal
     * 
     * Requirements: 37.1, 37.4, 37.5, 37.6
     * - 37.1: Expose GET /api/portal-platform/v1/recommendations/banner endpoint
     * - 37.4: Support banner targeting by portal type (student, enterprise, college)
     * - 37.5: Support banner scheduling with start_date and end_date
     * - 37.6: Return only active banners (current date between start_date and end_date)
     * 
     * @param portal Filter by target portal (student, enterprise, college, all), null for all
     * @return List of active banners
     */
    public List<BannerDTO> getBanners(String portal) {
        log.debug("Getting banners for portal: {}", portal);

        // Use cache key based on portal parameter
        String cacheKey = String.format(CacheConstants.KEY_PLATFORM_BANNERS, portal != null ? portal : "all");

        return cacheService.getOrSet(
            cacheKey,
            CacheConstants.TTL_BANNERS,
            CacheConstants.TTL_BANNERS_UNIT,
            () -> {
                log.debug("Cache miss for banners, querying database");

                // Build query for active banners
                LambdaQueryWrapper<RecommendationBanner> queryWrapper = new LambdaQueryWrapper<RecommendationBanner>()
                        .eq(RecommendationBanner::getStatus, 1); // 1 = active

                // Filter by target portal if specified
                if (portal != null && !portal.isEmpty() && !"all".equals(portal)) {
                    // Return banners targeted to specific portal OR 'all' portals
                    queryWrapper.and(wrapper -> wrapper
                            .eq(RecommendationBanner::getTargetPortal, portal)
                            .or()
                            .eq(RecommendationBanner::getTargetPortal, "all")
                    );
                }

                // Filter by date range - only return banners active today
                LocalDate today = LocalDate.now();
                queryWrapper.le(RecommendationBanner::getStartDate, today)
                        .ge(RecommendationBanner::getEndDate, today);

                // Order by sort_order ascending
                queryWrapper.orderByAsc(RecommendationBanner::getSortOrder);

                List<RecommendationBanner> banners = bannerMapper.selectList(queryWrapper);

                log.debug("Retrieved {} active banners for portal: {}", banners.size(), portal);

                // Convert to DTO
                return banners.stream()
                        .map(this::convertToBannerDTO)
                        .toList();
            }
        );
    }

    /**
     * 保存推荐横幅
     * Save recommendation banner (create or update)
     * 
     * Requirements: 37.2, 37.3
     * - 37.2: Expose POST /api/portal-platform/v1/recommendations/banner endpoint
     * - 37.3: Validate required fields (title, image_url, link_url, target_portal)
     * 
     * @param request Banner save request
     */
    @Transactional
    public void saveBanner(SaveBannerRequest request) {
        log.debug("Saving banner: {}", request.getTitle());

        // Validate required fields
        validateBannerRequest(request);

        // Create or update banner
        RecommendationBanner banner;
        if (request.getId() != null) {
            // Update existing banner
            banner = bannerMapper.selectById(request.getId());
            if (banner == null) {
                throw new IllegalArgumentException("Banner not found with id: " + request.getId());
            }
            log.debug("Updating existing banner with id: {}", request.getId());
        } else {
            // Create new banner
            banner = new RecommendationBanner();
            log.debug("Creating new banner");
        }

        // Set banner fields
        banner.setTitle(request.getTitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setLinkUrl(request.getLinkUrl());
        banner.setTargetPortal(request.getTargetPortal());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        banner.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        banner.setStatus(request.getStatus() != null ? request.getStatus() : 1);

        // Save to database
        if (request.getId() != null) {
            banner.setUpdatedAt(OffsetDateTime.now());
            bannerMapper.updateById(banner);
        } else {
            bannerMapper.insert(banner);
        }

        // Invalidate all banner caches
        invalidateBannerCache();

        log.info("Banner saved successfully: {} (id: {})", banner.getTitle(), banner.getId());
    }

    /**
     * Validate banner request fields
     * 
     * @param request Banner save request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBannerRequest(SaveBannerRequest request) {
        // Requirement 37.3: Validate required fields
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("title is required");
        }
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("image_url is required");
        }
        if (request.getLinkUrl() == null || request.getLinkUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("link_url is required");
        }
        if (request.getTargetPortal() == null || request.getTargetPortal().trim().isEmpty()) {
            throw new IllegalArgumentException("target_portal is required");
        }

        // Validate target_portal value
        String portal = request.getTargetPortal();
        if (!"student".equals(portal) && !"enterprise".equals(portal) && 
            !"college".equals(portal) && !"all".equals(portal)) {
            throw new IllegalArgumentException(
                "target_portal must be one of: student, enterprise, college, all");
        }

        // Validate date range
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("start_date is required");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("end_date is required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("end_date must be after or equal to start_date");
        }

        log.debug("Banner request validation passed");
    }

    /**
     * Invalidate all banner caches
     */
    private void invalidateBannerCache() {
        log.debug("Invalidating banner caches");
        cacheService.invalidatePattern(CacheConstants.PATTERN_PLATFORM_BANNERS);
    }

    /**
     * Convert RecommendationBanner entity to BannerDTO
     * 
     * @param banner Banner entity
     * @return Banner DTO
     */
    private BannerDTO convertToBannerDTO(RecommendationBanner banner) {
        return new BannerDTO(
                banner.getId(),
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getLinkUrl(),
                banner.getTargetPortal(),
                banner.getStartDate(),
                banner.getEndDate(),
                banner.getSortOrder(),
                banner.getStatus()
        );
    }

    // ==================== Top List Methods ====================

    /**
     * 获取推荐榜单
     * Get top list by list type
     * 
     * Requirements: 38.1, 38.3
     * - 38.1: Expose GET /api/portal-platform/v1/recommendations/top-list endpoint
     * - 38.3: Accept list_type query parameter with values "mentor", "course", "project"
     * 
     * @param listType List type (mentor, course, project)
     * @return Top list DTO with ordered item IDs
     */
    public TopListDTO getTopList(String listType) {
        log.debug("Getting top list for type: {}", listType);

        // Validate list type
        validateListType(listType);

        // Use cache key based on list type
        String cacheKey = String.format(CacheConstants.KEY_PLATFORM_TOP_LIST, listType);

        return cacheService.getOrSet(
            cacheKey,
            CacheConstants.TTL_TOP_LIST,
            CacheConstants.TTL_TOP_LIST_UNIT,
            () -> {
                log.debug("Cache miss for top list, querying database");

                // Query by list type (unique constraint ensures only one record per type)
                LambdaQueryWrapper<RecommendationTopList> queryWrapper = new LambdaQueryWrapper<RecommendationTopList>()
                        .eq(RecommendationTopList::getListType, listType);

                RecommendationTopList topList = topListMapper.selectOne(queryWrapper);

                if (topList == null) {
                    log.debug("No top list found for type: {}, returning empty list", listType);
                    return new TopListDTO(listType, new ArrayList<>());
                }

                // Parse JSON array of item IDs
                List<Long> itemIds = parseItemIds(topList.getItemIds());

                log.debug("Retrieved top list for type: {} with {} items", listType, itemIds.size());

                return new TopListDTO(listType, itemIds);
            }
        );
    }

    /**
     * 保存推荐榜单
     * Save top list (create or update)
     * 
     * Requirements: 38.2, 38.4, 38.5, 38.6
     * - 38.2: Expose POST /api/portal-platform/v1/recommendations/top-list endpoint
     * - 38.4: Validate required fields (list_type, item_ids)
     * - 38.5: Support ordering items by position in item_ids array
     * - 38.6: Limit each top list to maximum 10 items
     * 
     * @param request Top list save request
     */
    @Transactional
    public void saveTopList(SaveTopListRequest request) {
        log.debug("Saving top list: {}", request.getListType());

        // Validate request
        validateTopListRequest(request);

        // Check if top list already exists for this type
        LambdaQueryWrapper<RecommendationTopList> queryWrapper = new LambdaQueryWrapper<RecommendationTopList>()
                .eq(RecommendationTopList::getListType, request.getListType());

        RecommendationTopList topList = topListMapper.selectOne(queryWrapper);

        // Convert item IDs to JSON array string
        String itemIdsJson = serializeItemIds(request.getItemIds());

        if (topList != null) {
            // Update existing top list
            log.debug("Updating existing top list for type: {}", request.getListType());
            topList.setItemIds(itemIdsJson);
            topList.setUpdatedAt(OffsetDateTime.now());
            topListMapper.updateById(topList);
        } else {
            // Create new top list
            log.debug("Creating new top list for type: {}", request.getListType());
            topList = new RecommendationTopList();
            topList.setListType(request.getListType());
            topList.setItemIds(itemIdsJson);
            topList.setUpdatedAt(OffsetDateTime.now());
            topListMapper.insert(topList);
        }

        // Invalidate cache for this list type
        invalidateTopListCache(request.getListType());

        log.info("Top list saved successfully: {} with {} items", request.getListType(), request.getItemIds().size());
    }

    /**
     * Validate list type parameter
     * 
     * @param listType List type to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateListType(String listType) {
        if (listType == null || listType.trim().isEmpty()) {
            throw new IllegalArgumentException("list_type is required");
        }

        if (!"mentor".equals(listType) && !"course".equals(listType) && !"project".equals(listType)) {
            throw new IllegalArgumentException("list_type must be one of: mentor, course, project");
        }
    }

    /**
     * Validate top list request fields
     * 
     * @param request Top list save request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTopListRequest(SaveTopListRequest request) {
        // Requirement 38.4: Validate required fields
        if (request.getListType() == null || request.getListType().trim().isEmpty()) {
            throw new IllegalArgumentException("list_type is required");
        }

        validateListType(request.getListType());

        if (request.getItemIds() == null) {
            throw new IllegalArgumentException("item_ids is required");
        }

        // Requirement 38.6: Limit to maximum 10 items
        if (request.getItemIds().size() > 10) {
            throw new IllegalArgumentException("item_ids cannot contain more than 10 items");
        }

        // Validate all item IDs are positive
        for (Long itemId : request.getItemIds()) {
            if (itemId == null || itemId <= 0) {
                throw new IllegalArgumentException("All item IDs must be positive numbers");
            }
        }

        log.debug("Top list request validation passed");
    }

    /**
     * Parse JSON array string to List<Long>
     * 
     * @param itemIdsJson JSON array string
     * @return List of item IDs
     */
    private List<Long> parseItemIds(String itemIdsJson) {
        try {
            return objectMapper.readValue(itemIdsJson, new TypeReference<List<Long>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse item IDs JSON: {}", itemIdsJson, e);
            return new ArrayList<>();
        }
    }

    /**
     * Serialize List<Long> to JSON array string
     * 
     * @param itemIds List of item IDs
     * @return JSON array string
     */
    private String serializeItemIds(List<Long> itemIds) {
        try {
            return objectMapper.writeValueAsString(itemIds);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize item IDs to JSON", e);
            throw new RuntimeException("Failed to serialize item IDs", e);
        }
    }

    /**
     * Invalidate top list cache for specific list type
     * 
     * @param listType List type
     */
    private void invalidateTopListCache(String listType) {
        log.debug("Invalidating top list cache for type: {}", listType);
        String cacheKey = String.format(CacheConstants.KEY_PLATFORM_TOP_LIST, listType);
        cacheService.invalidate(cacheKey);
    }
}
