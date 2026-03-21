package com.zhitu.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhitu.platform.dto.BannerDTO;
import com.zhitu.platform.dto.SaveBannerRequest;
import com.zhitu.platform.dto.SaveTopListRequest;
import com.zhitu.platform.dto.TopListDTO;
import com.zhitu.platform.service.PlatformRecommendationService;
import com.zhitu.platform.service.PlatformService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 平台门户控制器测试
 * Platform Portal Controller Tests
 */
@WebMvcTest(PlatformPortalController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PlatformPortalController Tests")
class PlatformPortalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlatformRecommendationService recommendationService;

    @MockBean
    private PlatformService platformService;

    private BannerDTO createTestBannerDTO(Long id, String title, String portal) {
        return new BannerDTO(
                id,
                title,
                "https://example.com/image.jpg",
                "https://example.com/link",
                portal,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                0,
                1
        );
    }

    // ==================== GET /api/portal-platform/v1/recommendations/banner ====================

    @Test
    @DisplayName("Should return banners for specific portal")
    void testGetRecommendationBanners_SpecificPortal() throws Exception {
        // Arrange
        List<BannerDTO> banners = Arrays.asList(
                createTestBannerDTO(1L, "Student Banner", "student"),
                createTestBannerDTO(2L, "All Portal Banner", "all")
        );

        when(recommendationService.getBanners("student")).thenReturn(banners);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "student"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Student Banner"))
                .andExpect(jsonPath("$.data[0].targetPortal").value("student"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].title").value("All Portal Banner"))
                .andExpect(jsonPath("$.data[1].targetPortal").value("all"));

        verify(recommendationService).getBanners("student");
    }

    @Test
    @DisplayName("Should return all banners when portal parameter is not provided")
    void testGetRecommendationBanners_NoPortalParameter() throws Exception {
        // Arrange
        List<BannerDTO> banners = Arrays.asList(
                createTestBannerDTO(1L, "Banner 1", "student"),
                createTestBannerDTO(2L, "Banner 2", "enterprise")
        );

        when(recommendationService.getBanners(null)).thenReturn(banners);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(2)));

        verify(recommendationService).getBanners(null);
    }

    @Test
    @DisplayName("Should return empty list when no banners found")
    void testGetRecommendationBanners_EmptyList() throws Exception {
        // Arrange
        when(recommendationService.getBanners("college")).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "college"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(0)));

        verify(recommendationService).getBanners("college");
    }

    @Test
    @DisplayName("Should return banners for enterprise portal")
    void testGetRecommendationBanners_EnterprisePortal() throws Exception {
        // Arrange
        List<BannerDTO> banners = Arrays.asList(
                createTestBannerDTO(1L, "Enterprise Banner", "enterprise")
        );

        when(recommendationService.getBanners("enterprise")).thenReturn(banners);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/banner")
                        .param("portal", "enterprise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].targetPortal").value("enterprise"));

        verify(recommendationService).getBanners("enterprise");
    }

    // ==================== POST /api/portal-platform/v1/recommendations/banner ====================

    @Test
    @DisplayName("Should create new banner successfully")
    void testSaveRecommendationBanner_CreateNew() throws Exception {
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

        doNothing().when(recommendationService).saveBanner(any(SaveBannerRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveBanner(any(SaveBannerRequest.class));
    }

    @Test
    @DisplayName("Should update existing banner successfully")
    void testSaveRecommendationBanner_UpdateExisting() throws Exception {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setId(1L);
        request.setTitle("Updated Banner");
        request.setImageUrl("https://example.com/new-image.jpg");
        request.setLinkUrl("https://example.com/new-link");
        request.setTargetPortal("enterprise");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(14));
        request.setSortOrder(2);
        request.setStatus(0);

        doNothing().when(recommendationService).saveBanner(any(SaveBannerRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveBanner(any(SaveBannerRequest.class));
    }

    @Test
    @DisplayName("Should handle validation errors from service")
    void testSaveRecommendationBanner_ValidationError() throws Exception {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle(""); // Invalid: empty title
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));

        doThrow(new IllegalArgumentException("title is required"))
                .when(recommendationService).saveBanner(any(SaveBannerRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).saveBanner(any(SaveBannerRequest.class));
    }

    @Test
    @DisplayName("Should save banner with all portal types")
    void testSaveRecommendationBanner_AllPortalTypes() throws Exception {
        // Test each valid portal type
        String[] portalTypes = {"student", "enterprise", "college", "all"};

        for (String portalType : portalTypes) {
            SaveBannerRequest request = new SaveBannerRequest();
            request.setTitle("Banner for " + portalType);
            request.setImageUrl("https://example.com/image.jpg");
            request.setLinkUrl("https://example.com/link");
            request.setTargetPortal(portalType);
            request.setStartDate(LocalDate.now());
            request.setEndDate(LocalDate.now().plusDays(7));

            doNothing().when(recommendationService).saveBanner(any(SaveBannerRequest.class));

            // Act & Assert
            mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        verify(recommendationService, times(4)).saveBanner(any(SaveBannerRequest.class));
    }

    @Test
    @DisplayName("Should save banner with minimal required fields")
    void testSaveRecommendationBanner_MinimalFields() throws Exception {
        // Arrange
        SaveBannerRequest request = new SaveBannerRequest();
        request.setTitle("Minimal Banner");
        request.setImageUrl("https://example.com/image.jpg");
        request.setLinkUrl("https://example.com/link");
        request.setTargetPortal("student");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(7));
        // sortOrder and status are null (will use defaults)

        doNothing().when(recommendationService).saveBanner(any(SaveBannerRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/banner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveBanner(any(SaveBannerRequest.class));
    }

    // ==================== GET /api/portal-platform/v1/recommendations/top-list ====================

    @Test
    @DisplayName("Should return top list for mentor type")
    void testGetTopList_MentorType() throws Exception {
        // Arrange
        TopListDTO topList = new TopListDTO("mentor", Arrays.asList(1L, 2L, 3L, 4L, 5L));
        when(recommendationService.getTopList("mentor")).thenReturn(topList);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.listType").value("mentor"))
                .andExpect(jsonPath("$.data.itemIds", hasSize(5)))
                .andExpect(jsonPath("$.data.itemIds[0]").value(1))
                .andExpect(jsonPath("$.data.itemIds[1]").value(2))
                .andExpect(jsonPath("$.data.itemIds[2]").value(3))
                .andExpect(jsonPath("$.data.itemIds[3]").value(4))
                .andExpect(jsonPath("$.data.itemIds[4]").value(5));

        verify(recommendationService).getTopList("mentor");
    }

    @Test
    @DisplayName("Should return top list for course type")
    void testGetTopList_CourseType() throws Exception {
        // Arrange
        TopListDTO topList = new TopListDTO("course", Arrays.asList(10L, 20L, 30L));
        when(recommendationService.getTopList("course")).thenReturn(topList);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "course"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.listType").value("course"))
                .andExpect(jsonPath("$.data.itemIds", hasSize(3)))
                .andExpect(jsonPath("$.data.itemIds[0]").value(10))
                .andExpect(jsonPath("$.data.itemIds[1]").value(20))
                .andExpect(jsonPath("$.data.itemIds[2]").value(30));

        verify(recommendationService).getTopList("course");
    }

    @Test
    @DisplayName("Should return top list for project type")
    void testGetTopList_ProjectType() throws Exception {
        // Arrange
        TopListDTO topList = new TopListDTO("project", Arrays.asList(100L, 200L, 300L, 400L));
        when(recommendationService.getTopList("project")).thenReturn(topList);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "project"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.listType").value("project"))
                .andExpect(jsonPath("$.data.itemIds", hasSize(4)));

        verify(recommendationService).getTopList("project");
    }

    @Test
    @DisplayName("Should return empty top list when no items exist")
    void testGetTopList_EmptyList() throws Exception {
        // Arrange
        TopListDTO topList = new TopListDTO("mentor", Collections.emptyList());
        when(recommendationService.getTopList("mentor")).thenReturn(topList);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "mentor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.listType").value("mentor"))
                .andExpect(jsonPath("$.data.itemIds", hasSize(0)));

        verify(recommendationService).getTopList("mentor");
    }

    @Test
    @DisplayName("Should return top list with maximum 10 items")
    void testGetTopList_MaximumItems() throws Exception {
        // Arrange
        TopListDTO topList = new TopListDTO("course", 
                Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        when(recommendationService.getTopList("course")).thenReturn(topList);

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "course"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.itemIds", hasSize(10)));

        verify(recommendationService).getTopList("course");
    }

    @Test
    @DisplayName("Should handle validation error when listType is missing")
    void testGetTopList_MissingListType() throws Exception {
        // Act & Assert - missing required parameter should return 400
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list"))
                .andExpect(status().is4xxClientError());

        verify(recommendationService, never()).getTopList(any());
    }

    @Test
    @DisplayName("Should handle validation error from service for invalid listType")
    void testGetTopList_InvalidListType() throws Exception {
        // Arrange
        when(recommendationService.getTopList("invalid"))
                .thenThrow(new IllegalArgumentException("list_type must be one of: mentor, course, project"));

        // Act & Assert
        mockMvc.perform(get("/api/portal-platform/v1/recommendations/top-list")
                        .param("listType", "invalid"))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).getTopList("invalid");
    }

    // ==================== POST /api/portal-platform/v1/recommendations/top-list ====================

    @Test
    @DisplayName("Should save top list successfully")
    void testSaveTopList_Success() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));

        doNothing().when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should save top list with maximum 10 items")
    void testSaveTopList_MaximumItems() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));

        doNothing().when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should save top list with single item")
    void testSaveTopList_SingleItem() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("project");
        request.setItemIds(Collections.singletonList(100L));

        doNothing().when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should save top list with empty item list")
    void testSaveTopList_EmptyItemList() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(Collections.emptyList());

        doNothing().when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should save top list for all valid list types")
    void testSaveTopList_AllListTypes() throws Exception {
        // Test each valid list type
        String[] listTypes = {"mentor", "course", "project"};

        for (String listType : listTypes) {
            SaveTopListRequest request = new SaveTopListRequest();
            request.setListType(listType);
            request.setItemIds(Arrays.asList(1L, 2L, 3L));

            doNothing().when(recommendationService).saveTopList(any(SaveTopListRequest.class));

            // Act & Assert
            mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        verify(recommendationService, times(3)).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should handle validation error when listType is missing")
    void testSaveTopList_MissingListType() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setItemIds(Arrays.asList(1L, 2L, 3L));

        doThrow(new IllegalArgumentException("list_type is required"))
                .when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should handle validation error when itemIds is null")
    void testSaveTopList_NullItemIds() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("mentor");
        request.setItemIds(null);

        doThrow(new IllegalArgumentException("item_ids is required"))
                .when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should handle validation error when itemIds exceeds 10 items")
    void testSaveTopList_TooManyItems() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("course");
        request.setItemIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        doThrow(new IllegalArgumentException("item_ids cannot contain more than 10 items"))
                .when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }

    @Test
    @DisplayName("Should handle validation error for invalid listType")
    void testSaveTopList_InvalidListType() throws Exception {
        // Arrange
        SaveTopListRequest request = new SaveTopListRequest();
        request.setListType("invalid_type");
        request.setItemIds(Arrays.asList(1L, 2L, 3L));

        doThrow(new IllegalArgumentException("list_type must be one of: mentor, course, project"))
                .when(recommendationService).saveTopList(any(SaveTopListRequest.class));

        // Act & Assert
        mockMvc.perform(post("/api/portal-platform/v1/recommendations/top-list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());

        verify(recommendationService).saveTopList(any(SaveTopListRequest.class));
    }
}
