package com.motorclaim.portal.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motorclaim.portal.controller.ClaimRestController;
import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import com.motorclaim.portal.service.ClaimService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClaimRestController.class)
public class ClaimRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @Test
    @DisplayName("POST /api/claims should create claim and return 201 Created")
    void testSubmitClaimApiSuccess() throws Exception {
        ClaimSubmissionRequest request = new ClaimSubmissionRequest();
        request.setPolicyNumber("POL-123456");
        request.setClaimantName("Priya Verma");
        request.setClaimantEmail("priya@example.com");
        request.setClaimantPhone("9811223344");
        request.setVehicleNumber("DL01AB9999");
        request.setIncidentDate(LocalDate.now().minusDays(1));
        request.setIncidentLocation("Outer Ring Road, Delhi");
        request.setEstimatedAmount(new BigDecimal("12500.00"));
        request.setDescription("Side door scratch and dent caused during reversing in parking.");

        Claim savedClaim = new Claim();
        savedClaim.setId(10L);
        savedClaim.setClaimNumber("CLM-2026-X9Y8");
        savedClaim.setPolicyNumber(request.getPolicyNumber());
        savedClaim.setClaimantName(request.getClaimantName());
        savedClaim.setStatus(ClaimStatus.SUBMITTED);

        when(claimService.submitClaim(any(ClaimSubmissionRequest.class))).thenReturn(savedClaim);

        mockMvc.perform(post("/api/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.claimNumber").value("CLM-2026-X9Y8"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    @DisplayName("GET /api/claims/track/{num} should return claim if found")
    void testTrackClaimApiSuccess() throws Exception {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimNumber("CLM-2026-ABC1");
        claim.setStatus(ClaimStatus.APPROVED);

        when(claimService.getClaimByClaimNumber("CLM-2026-ABC1")).thenReturn(Optional.of(claim));

        mockMvc.perform(get("/api/claims/track/CLM-2026-ABC1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.claimNumber").value("CLM-2026-ABC1"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("PUT /api/claims/{id}/review should update status to APPROVED")
    void testReviewClaimApiSuccess() throws Exception {
        ReviewActionRequest reviewRequest = new ReviewActionRequest(ClaimStatus.APPROVED, "Verified by surveyor");

        Claim updated = new Claim();
        updated.setId(1L);
        updated.setStatus(ClaimStatus.APPROVED);
        updated.setReviewerComments("Verified by surveyor");

        when(claimService.reviewClaim(any(Long.class), any(ReviewActionRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/claims/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}
