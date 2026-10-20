package com.motorclaim.portal.unit;

import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import com.motorclaim.portal.repository.ClaimRepository;
import com.motorclaim.portal.service.ClaimServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private ClaimSubmissionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new ClaimSubmissionRequest();
        sampleRequest.setPolicyNumber("POL-998877");
        sampleRequest.setClaimantName("Aarav Sharma");
        sampleRequest.setClaimantEmail("aarav.sharma@example.com");
        sampleRequest.setClaimantPhone("9876543210");
        sampleRequest.setVehicleNumber("MH12AB1234");
        sampleRequest.setIncidentDate(LocalDate.now().minusDays(2));
        sampleRequest.setIncidentLocation("Pune Highway");
        sampleRequest.setEstimatedAmount(new BigDecimal("15000.00"));
        sampleRequest.setDescription("Rear bumper damage due to sudden braking by leading vehicle.");
    }

    @Test
    @DisplayName("Should successfully create a claim with status SUBMITTED and unique claim number")
    void testSubmitClaimSuccess() {
        when(claimRepository.findByClaimNumber(anyString())).thenReturn(Optional.empty());
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> {
            Claim claim = invocation.getArgument(0);
            claim.setId(1L);
            return claim;
        });

        Claim created = claimService.submitClaim(sampleRequest);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(created.getClaimNumber()).startsWith("CLM-");
        assertThat(created.getPolicyNumber()).isEqualTo("POL-998877");
        assertThat(created.getClaimantName()).isEqualTo("Aarav Sharma");

        verify(claimRepository, times(1)).save(any(Claim.class));
    }

    @Test
    @DisplayName("Should successfully approve a claim during review")
    void testReviewClaimApprove() {
        Claim existing = new Claim();
        existing.setId(5L);
        existing.setClaimNumber("CLM-2026-TEST1");
        existing.setStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewActionRequest reviewRequest = new ReviewActionRequest(ClaimStatus.APPROVED, "Approved after surveyor site inspection.");
        Claim updated = claimService.reviewClaim(5L, reviewRequest);

        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.APPROVED);
        assertThat(updated.getReviewerComments()).isEqualTo("Approved after surveyor site inspection.");
        verify(claimRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when reviewing non-existent claim ID")
    void testReviewClaimNotFound() {
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        ReviewActionRequest reviewRequest = new ReviewActionRequest(ClaimStatus.REJECTED, "Invalid documents");

        assertThrows(IllegalArgumentException.class, () -> claimService.reviewClaim(999L, reviewRequest));
    }
}
