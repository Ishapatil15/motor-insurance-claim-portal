package com.motorclaim.portal.service;

import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import com.motorclaim.portal.repository.ClaimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ClaimService handling business logic, validation,
 * audit trail, and persistence.
 */
@Service
@Transactional
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;

    @Autowired
    public ClaimServiceImpl(ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    @Override
    public Claim submitClaim(ClaimSubmissionRequest request) {
        Claim claim = new Claim();
        claim.setClaimNumber(generateUniqueClaimNumber());
        claim.setPolicyNumber(request.getPolicyNumber());
        claim.setClaimantName(request.getClaimantName());
        claim.setClaimantEmail(request.getClaimantEmail());
        claim.setClaimantPhone(request.getClaimantPhone());
        claim.setVehicleNumber(request.getVehicleNumber());
        claim.setIncidentDate(request.getIncidentDate());
        claim.setIncidentLocation(request.getIncidentLocation());
        claim.setEstimatedAmount(request.getEstimatedAmount());
        claim.setDescription(request.getDescription());
        claim.setStatus(ClaimStatus.SUBMITTED);
        claim.setReviewerComments("Initial claim submitted by policyholder. Awaiting review.");

        return claimRepository.save(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Claim> getAllClaims() {
        return claimRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Claim> getClaimByClaimNumber(String claimNumber) {
        if (claimNumber == null) return Optional.empty();
        return claimRepository.findByClaimNumber(claimNumber.trim().toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    @Override
    public Claim reviewClaim(Long id, ReviewActionRequest actionRequest) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found with ID: " + id));

        claim.setStatus(actionRequest.getStatus());
        claim.setReviewerComments(actionRequest.getReviewerComments());

        return claimRepository.save(claim);
    }

    @Override
    @Transactional(readOnly = true)
    public long getCountByStatus(ClaimStatus status) {
        return claimRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount() {
        return claimRepository.count();
    }

    /**
     * Generates a predictable yet unique claim reference number:
     * Format: CLM-YYYY-XXXX (e.g., CLM-2026-A1B2)
     */
    private String generateUniqueClaimNumber() {
        int year = LocalDate.now().getYear();
        String suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        String candidate = "CLM-" + year + "-" + suffix;
        while (claimRepository.findByClaimNumber(candidate).isPresent()) {
            suffix = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            candidate = "CLM-" + year + "-" + suffix;
        }
        return candidate;
    }
}
