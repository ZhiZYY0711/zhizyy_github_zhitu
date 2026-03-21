package com.zhitu.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.common.redis.service.CacheService;
import com.zhitu.platform.dto.BannerDTO;
import com.zhitu.platform.dto.SaveBannerRequest;
import com.zhitu.platform.dto.SaveTopListRequest;
import com.zhitu.platform.dto.TopListDTO;
import com.zhitu.platform.entity.RecommendationBanner;
import com.zhitu.platform.entity.RecommendationTopList;
import com.zhitu.platform.mapper.RecommendationBannerMapper;
import com.zhitu.platform.mapper.RecommendationTopListMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 平台推荐服务测试
 * Platform Recommendation Service Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformRecommendationService Tests")
class PlatformRecommendationServiceTest {

    @Mock
    private RecommendationBannerMapper bannerMapper;

    @Mock
    private RecommendationTopListMapper topListMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PlatformRecommendationService service;

    private RecommendationBanner createTestBanner(Long id, String title, String portal, 
                                                   LocalDate startDate, LocalDate endDate) {
        RecommendationBanner banner = new RecommendationBanner();
        banner.setId(id);
        banner.setTitle(title);
        banner.setImageUrl("https://example.com/image.jpg");
        banner.setLinkUrl("https://example.com/link");
        banner.setTargetPortal(portal);
        banner.setStartDate(startDate);
        banner.setEndDate(endDate);
        banner.setSortOrder(0);
        banner.setStatus(1);
        return banner;
    }

    @BeforeEach
    void setUp() {
        // Setup cache service to execute supplier directly (cache miss simulation)
        lenient().when(cacheService.getOrSet(anyString(), anyLong(), any(TimeUnit.class), any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(3);
                    return supplier.get();
                });
    }

    // ==================== getBanners Tests ====================

    @Test
    @DisplayName("Should return active banners for specific portal")
    void testGetBanners_SpecificPortal() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        RecommendationBanner banner1 = createTestBanner(1L, "Student Banner", "student", yesterday, tomorrow);
        RecommendationBanner banner2 = createTestBanner(2L, "All Portal Banner", "all", yesterday, tomorrow);

        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(banner1, banner2));

        // Act
        List<BannerDTO> result = service.getBanners("student");

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Student Banner");
        assertThat(result.get(1).getTitle()).isEqualTo("All Portal Banner");

        // Verify cache was used
        verify(cacheService).getOrSet(
                eq("platform:banners:student"),
                anyLong(),
                any(TimeUnit.class),
                any()
        );
    }

    @Test
    @DisplayName("Should return only active banners within date range")
    void testGetBanners_OnlyActiveBannersInDateRange() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);
        LocalDate lastWeek = today.minusDays(7);
        LocalDate nextWeek = today.plusDays(7);

        // Active banner (today is within range)
        RecommendationBanner activeBanner = createTestBanner(1L, "Active Banner", "student", yesterday, tomorrow);

        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(activeBanner));

        // Act
        List<BannerDTO> result = service.getBanners("student");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Active Banner");
    }

    @Test
    @DisplayName("Should return empty list when no active banners")
    void testGetBanners_NoActiveBanners() {
        // Arrange
        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList());

        // Act
        List<BannerDTO> result = service.getBanners("student");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all portal banners when portal is null")
    void testGetBanners_NullPortal() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        RecommendationBanner banner1 = createTestBanner(1L, "Student Banner", "student", yesterday, tomorrow);
        RecommendationBanner banner2 = createTestBanner(2L, "Enterprise Banner", "enterprise", yesterday, tomorrow);

        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(banner1, banner2));

        // Act
        List<BannerDTO> result = service.getBanners(null);

        // Assert
        assertThat(result).hasSize(2);

        // Verify cache key uses "all" for null portal
        verify(cacheService).getOrSet(
                eq("platform:banners:all"),
                anyLong(),
                any(TimeUnit.class),
                any()
        );
    }

    @Test
    @DisplayName("Should return banners for 'all' portal type")
    void testGetBanners_AllPortalType() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        RecommendationBanner banner = createTestBanner(1L, "All Portal Banner", "all", yesterday, tomorrow);

        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(banner));

        // Act
        List<BannerDTO> result = service.getBanners("all");

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetPortal()).isEqualTo("all");
    }

    @Test
    @DisplayName("Should use cache with 30-minute TTL")
    void testGetBanners_CacheTTL() {
        // Arrange
        when(bannerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList());

        // Act
        service.getBanners("student");

        // Assert - verify cache TTL is 30 minutes
        verify(cacheService).getOrSet(
                anyString(),
                eq(30L),
                eq(TimeUnit.MINUTES),
                any()
        );
    }

    // ==================== saveBanner Tests ====================

    @Test
    @DisplayName("Should create new banner successfully")
    void testSaveBanner_CreateNew() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("New Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        request.setSortOrder(1);
        request.setStatus(1);

        when(bannerMapper.insert(any(RecommendationBanner.class))).thenReturn(1);

        // Act
        service.saveBanner(request);

        // Assert
        ArgumentCaptor<RecommendationBanner> captor = ArgumentCaptor.forClass(RecommendationBanner.class);
        verify(bannerMapper).insert(captor.capture());

        RecommendationBanner savedBanner = captor.getValue();
        assertThat(savedBanner.getTitle()).isEqualTo("New Banner");
        assertThat(savedBanner.getImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(savedBanner.getLinkUrl()).isEqualTo("https://example.com/link");
        assertThat(savedBanner.getTargetPortal()).isEqualTo("student");
        assertThat(savedBanner.getSortOrder()).isEqualTo(1);
        assertThat(savedBanner.getStatus()).isEqualTo(1);

        // Verify cache invalidation
        verify(cacheService).invalidatePattern("platform:banners:*");
    }

    @Test
    @DisplayName("Should update existing banner successfully")
    void testSaveBanner_UpdateExisting() {
        // Arrange
        Long bannerId = 1L;
        RecommendationBanner existingBanner = createTestBanner(
                bannerId, "Old Title", "student", LocalDate.now(), LocalDate.now().plusDays(7));

        SaveBannerRequest request = new SaveBannerRequest();
        request.setId(bannerId);
        request.setTitle("Updated Banner");
        request.setImageUrl("https://example.com/new-image.jpg");
        request.setLinkUrl("https://example.com/new-link");
        request.setTargetPortal("enterprise");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(14));
        request.setSortOrder(2);
        request.setStatus(0);

        when(bannerMapper.selectById(bannerId)).thenReturn(existingBanner);
        when(bannerMapper.updateById(any(RecommendationBanner.class))).thenReturn(1);

        // Act
        service.saveBanner(request);

        // Assert
        ArgumentCaptor<RecommendationBanner> captor = ArgumentCaptor.forClass(RecommendationBanner.class);
        verify(bannerMapper).updateById(captor.capture());

        RecommendationBanner updatedBanner = captor.getValue();
        assertThat(updatedBanner.getId()).isEqualTo(bannerId);
        assertThat(updatedBanner.getTitle()).isEqualTo("Updated Banner");
        assertThat(updatedBanner.getTargetPortal()).isEqualTo("enterprise");
        assertThat(updatedBanner.getSortOrder()).isEqualTo(2);
        assertThat(updatedBanner.getStatus()).isEqualTo(0);

        // Verify cache invalidation
        verify(cacheService).invalidatePattern("platform:banners:*");
    }

    @Test
    @DisplayName("Should use default values when optional fields are null")
    void testSaveBanner_DefaultValues() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        // sortOrder and status are null

        when(bannerMapper.insert(any(RecommendationBanner.class))).thenReturn(1);

        // Act
        service.saveBanner(request);

        // Assert
        ArgumentCaptor<RecommendationBanner> captor = ArgumentCaptor.forClass(RecommendationBanner.class);
        verify(bannerMapper).insert(captor.capture());

        RecommendationBanner savedBanner = captor.getValue();
        assertThat(savedBanner.getSortOrder()).isEqualTo(0); // Default value
        assertThat(savedBanner.getStatus()).isEqualTo(1); // Default value
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Should throw exception when title is missing")
    void testSaveBanner_MissingTitle() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is required");

        verify(bannerMapper, never()).insert(any(RecommendationBanner.class));
        verify(bannerMapper, never()).updateById(any(RecommendationBanner.class));
    }

    @Test
    @DisplayName("Should throw exception when image_url is missing")
    void testSaveBanner_MissingImageUrl() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("image_url is required");
    }

    @Test
    @DisplayName("Should throw exception when link_url is missing")
    void testSaveBanner_MissingLinkUrl() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("link_url is required");
    }

    @Test
    @DisplayName("Should throw exception when target_portal is missing")
    void testSaveBanner_MissingTargetPortal() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target_portal is required");
    }

    @Test
    @DisplayName("Should throw exception when target_portal is invalid")
    void testSaveBanner_InvalidTargetPortal() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("invalid_portal");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("target_portal must be one of: student, enterprise, college, all");
    }

    @Test
    @DisplayName("Should throw exception when start_date is missing")
    void testSaveBanner_MissingStartDate() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setEndDate(LocalDate.now().plusDays(7));

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start_date is required");
    }

    @Test
    @DisplayName("Should throw exception when end_date is missing")
    void testSaveBanner_MissingEndDate() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end_date is required");
    }

    @Test
    @DisplayName("Should throw exception when end_date is before start_date")
    void testSaveBanner_InvalidDateRange() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now().plusDays(7));
        request.setEndDate(LocalDate.now()); // Before start date

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("end_date must be after or equal to start_date");
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent banner")
    void testSaveBanner_UpdateNonExistent() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setId(999L);
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        when(bannerMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> service.saveBanner(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Banner not found with id: 999");
    }

    @Test
    @DisplayName("Should invalidate cache after saving banner")
    void testSaveBanner_CacheInvalidation() {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        when(bannerMapper.insert(any(RecommendationBanner.class))).thenReturn(1);

        // Act
        service.saveBanner(request);

        // Assert - verify cache pattern deletion
        verify(cacheService).invalidatePattern("platform:banners:*");
    }

    // ==================== getTopList Tests ====================

    @Test
    @DisplayName("Should return top list for mentor type")
    void testGetTopList_MentorType() {
        // Arrange
        RecommendationTopList topList = new RecommendationTopList();
        topList.setId(1L);
        topList.setListType("mentor");
        topList.setItemIds("[1,2,3,4,5]");

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(topList);

        // Act
        TopListDTO result = service.getTopList("mentor");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getListType()).isEqualTo("mentor");
        assertThat(result.getItemIds()).containsExactly(1L, 2L, 3L, 4L, 5L);

        // Verify cache was used
        verify(cacheService).getOrSet(
                eq("platform:toplist:mentor"),
                eq(1L),
                eq(TimeUnit.HOURS),
                any()
        );
    }

    @Test
    @DisplayName("Should return top list for course type")
    void testGetTopList_CourseType() {
        // Arrange
        RecommendationTopList topList = new RecommendationTopList();
        topList.setId(2L);
        topList.setListType("course");
        topList.setItemIds("[10,20,30]");

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(topList);

        // Act
        TopListDTO result = service.getTopList("course");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getListType()).isEqualTo("course");
        assertThat(result.getItemIds()).containsExactly(10L, 20L, 30L);
    }

    @Test
    @DisplayName("Should return top list for project type")
    void testGetTopList_ProjectType() {
        // Arrange
        RecommendationTopList topList = new RecommendationTopList();
        topList.setId(3L);
        topList.setListType("project");
        topList.setItemIds("[100,200,300,400]");

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(topList);

        // Act
        TopListDTO result = service.getTopList("project");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getListType()).isEqualTo("project");
        assertThat(result.getItemIds()).containsExactly(100L, 200L, 300L, 400L);
    }

    @Test
    @DisplayName("Should return empty list when no top list exists")
    void testGetTopList_NoTopListExists() {
        // Arrange
        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        TopListDTO result = service.getTopList("mentor");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getListType()).isEqualTo("mentor");
        assertThat(result.getItemIds()).isEmpty();
    }

    @Test
    @DisplayName("Should throw exception when list_type is null")
    void testGetTopList_NullListType() {
        // Act & Assert
        assertThatThrownBy(() -> service.getTopList(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list_type is required");

        verify(topListMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("Should throw exception when list_type is empty")
    void testGetTopList_EmptyListType() {
        // Act & Assert
        assertThatThrownBy(() -> service.getTopList(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list_type is required");
    }

    @Test
    @DisplayName("Should throw exception when list_type is invalid")
    void testGetTopList_InvalidListType() {
        // Act & Assert
        assertThatThrownBy(() -> service.getTopList("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list_type must be one of: mentor, course, project");
    }

    @Test
    @DisplayName("Should use cache with 1-hour TTL")
    void testGetTopList_CacheTTL() {
        // Arrange
        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act
        service.getTopList("mentor");

        // Assert - verify cache TTL is 1 hour
        verify(cacheService).getOrSet(
                anyString(),
                eq(1L),
                eq(TimeUnit.HOURS),
                any()
        );
    }

    // ==================== saveTopList Tests ====================

    @Test
    @DisplayName("Should create new top list successfully")
    void testSaveTopList_CreateNew() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).insert(captor.capture());

        RecommendationTopList savedTopList = captor.getValue();
        assertThat(savedTopList.getListType()).isEqualTo("mentor");
        assertThat(savedTopList.getItemIds()).isEqualTo("[1,2,3,4,5]");

        // Verify cache invalidation
        verify(cacheService).invalidate("platform:toplist:mentor");
    }

    @Test
    @DisplayName("Should update existing top list successfully")
    void testSaveTopList_UpdateExisting() {
        // Arrange
        RecommendationTopList existingTopList = new RecommendationTopList();
        existingTopList.setId(1L);
        existingTopList.setListType("course");
        existingTopList.setItemIds("[10,20,30]");

        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Arrays.asList(40L, 50L, 60L, 70L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingTopList);
        when(topListMapper.updateById(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).updateById(captor.capture());

        RecommendationTopList updatedTopList = captor.getValue();
        assertThat(updatedTopList.getId()).isEqualTo(1L);
        assertThat(updatedTopList.getListType()).isEqualTo("course");
        assertThat(updatedTopList.getItemIds()).isEqualTo("[40,50,60,70]");

        // Verify cache invalidation
        verify(cacheService).invalidate("platform:toplist:course");
    }

    @Test
    @DisplayName("Should save top list with maximum 10 items")
    void testSaveTopList_MaximumItems() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("project");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).insert(captor.capture());

        RecommendationTopList savedTopList = captor.getValue();
        assertThat(savedTopList.getItemIds()).isEqualTo("[1,2,3,4,5,6,7,8,9,10]");
    }

    @Test
    @DisplayName("Should save top list with single item")
    void testSaveTopList_SingleItem() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Collections.singletonList(100L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).insert(captor.capture());

        RecommendationTopList savedTopList = captor.getValue();
        assertThat(savedTopList.getItemIds()).isEqualTo("[100]");
    }

    @Test
    @DisplayName("Should save top list with empty item list")
    void testSaveTopList_EmptyItemList() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Collections.emptyList());

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).insert(captor.capture());

        RecommendationTopList savedTopList = captor.getValue();
        assertThat(savedTopList.getItemIds()).isEqualTo("[]");
    }

    @Test
    @DisplayName("Should throw exception when list_type is missing")
    void testSaveTopList_MissingListType() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setItemIds(Arrays.asList(1L, 2L, 3L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list_type is required");

        verify(topListMapper, never()).insert(any(RecommendationTopList.class));
        verify(topListMapper, never()).updateById(any(RecommendationTopList.class));
    }

    @Test
    @DisplayName("Should throw exception when list_type is invalid")
    void testSaveTopList_InvalidListType() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("invalid_type");
        request.setItemIds(Arrays.asList(1L, 2L, 3L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("list_type must be one of: mentor, course, project");
    }

    @Test
    @DisplayName("Should throw exception when item_ids is null")
    void testSaveTopList_NullItemIds() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(null);

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("item_ids is required");
    }

    @Test
    @DisplayName("Should throw exception when item_ids exceeds 10 items")
    void testSaveTopList_TooManyItems() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("project");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("item_ids cannot contain more than 10 items");

        verify(topListMapper, never()).insert(any(RecommendationTopList.class));
        verify(topListMapper, never()).updateById(any(RecommendationTopList.class));
    }

    @Test
    @DisplayName("Should throw exception when item_ids contains null")
    void testSaveTopList_ItemIdsContainsNull() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Arrays.asList(1L, null, 3L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All item IDs must be positive numbers");
    }

    @Test
    @DisplayName("Should throw exception when item_ids contains zero")
    void testSaveTopList_ItemIdsContainsZero() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Arrays.asList(1L, 0L, 3L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All item IDs must be positive numbers");
    }

    @Test
    @DisplayName("Should throw exception when item_ids contains negative number")
    void testSaveTopList_ItemIdsContainsNegative() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("project");
        request.setItemIds(Arrays.asList(1L, -5L, 3L));

        // Act & Assert
        assertThatThrownBy(() -> service.saveTopList(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All item IDs must be positive numbers");
    }

    @Test
    @DisplayName("Should invalidate cache after saving top list")
    void testSaveTopList_CacheInvalidation() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Arrays.asList(1L, 2L, 3L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert - verify specific cache key deletion
        verify(cacheService).invalidate("platform:toplist:mentor");
    }

    @Test
    @DisplayName("Should preserve item order when saving top list")
    void testSaveTopList_PreserveItemOrder() {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Arrays.asList(5L, 3L, 8L, 1L, 9L));

        when(topListMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(topListMapper.insert(any(RecommendationTopList.class))).thenReturn(1);

        // Act
        service.saveTopList(request);

        // Assert - verify order is preserved in JSON
        ArgumentCaptor<RecommendationTopList> captor = ArgumentCaptor.forClass(RecommendationTopList.class);
        verify(topListMapper).insert(captor.capture());

        RecommendationTopList savedTopList = captor.getValue();
        assertThat(savedTopList.getItemIds()).isEqualTo("[5,3,8,1,9]");
    }
}

