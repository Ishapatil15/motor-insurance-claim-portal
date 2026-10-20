package com.motorclaim.portal.service;

import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Motor Insurance Claim management.
 */
public interface ClaimService {

    Claim submitClaim(ClaimSubmissionRequest request);

    List<Claim> getAllClaims();

    Optional<Claim> getClaimById(Long id);

    Optional<Claim> getClaimByClaimNumber(String claimNumber);

    List<Claim> getClaimsByStatus(ClaimStatus status);

    Claim reviewClaim(Long id, ReviewActionRequest actionRequest);

    long getCountByStatus(ClaimStatus status);

    long getTotalCount();
}
