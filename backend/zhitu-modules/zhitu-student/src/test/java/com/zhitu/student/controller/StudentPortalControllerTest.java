package com.zhitu.student.controller;

import com.zhitu.common.core.result.Result;
import com.zhitu.student.dto.RecommendationDTO;
import com.zhitu.student.service.StudentPortalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentPortalControllerTest {

    @Mock
    private StudentPortalService studentPortalService;

    @InjectMocks
    private StudentPortalController studentPortalController;

    @Test
    void getRecommendations_shouldReturnSuccessWithAllRecommendations() {
        // Given
        RecommendationDTO rec1 = new RecommendationDTO(
                1L, "project", 101L, new BigDecimal("95.5"), 
                "Based on your Java skills", OffsetDateTime.now()
        );
        RecommendationDTO rec2 = new RecommendationDTO(
                2L, "job", 201L, new BigDecimal("88.0"), 
                "Matches your experience", OffsetDateTime.now()
        );

        List<RecommendationDTO> recommendations = Arrays.asList(rec1, rec2);
        when(studentPortalService.getRecommendations("all")).thenReturn(recommendations);

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("all");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().size());

        verify(studentPortalService).getRecommendations("all");
    }

    @Test
    void getRecommendations_shouldReturnSuccessWithProjectRecommendations() {
        // Given
        RecommendationDTO rec1 = new RecommendationDTO(
                1L, "project", 101L, new BigDecimal("95.5"), 
                "Based on your Java skills", OffsetDateTime.now()
        );

        List<RecommendationDTO> recommendations = Collections.singletonList(rec1);
        when(studentPortalService.getRecommendations("project")).thenReturn(recommendations);

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("project");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("project", result.getData().get(0).getRecType());

        verify(studentPortalService).getRecommendations("project");
    }

    @Test
    void getRecommendations_shouldReturnSuccessWithJobRecommendations() {
        // Given
        RecommendationDTO rec1 = new RecommendationDTO(
                2L, "job", 201L, new BigDecimal("88.0"), 
                "Matches your experience", OffsetDateTime.now()
        );

        List<RecommendationDTO> recommendations = Collections.singletonList(rec1);
        when(studentPortalService.getRecommendations("job")).thenReturn(recommendations);

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("job");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("job", result.getData().get(0).getRecType());

        verify(studentPortalService).getRecommendations("job");
    }

    @Test
    void getRecommendations_shouldReturnSuccessWithCourseRecommendations() {
        // Given
        RecommendationDTO rec1 = new RecommendationDTO(
                3L, "course", 301L, new BigDecimal("92.0"), 
                "Recommended for skill development", OffsetDateTime.now()
        );

        List<RecommendationDTO> recommendations = Collections.singletonList(rec1);
        when(studentPortalService.getRecommendations("course")).thenReturn(recommendations);

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("course");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals("course", result.getData().get(0).getRecType());

        verify(studentPortalService).getRecommendations("course");
    }

    @Test
    void getRecommendations_shouldReturnEmptyListWhenNoRecommendations() {
        // Given
        when(studentPortalService.getRecommendations("all")).thenReturn(Collections.emptyList());

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("all");

        // Then
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());

        verify(studentPortalService).getRecommendations("all");
    }

    @Test
    void getRecommendations_shouldUseDefaultTypeWhenNotProvided() {
        // Given
        when(studentPortalService.getRecommendations("all")).thenReturn(Collections.emptyList());

        // When
        Result<List<RecommendationDTO>> result = studentPortalController.getRecommendations("all");

        // Then
        assertNotNull(result);
        verify(studentPortalService).getRecommendations("all");
    }
}
