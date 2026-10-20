package com.motorclaim.portal.controller;

import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import com.motorclaim.portal.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing API endpoints for the Motor Insurance Claim Portal.
 * Enables automated integration tests, mobile app support, and DevOps verification.
 */
@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimRestController {

    private final ClaimService claimService;

    @Autowired
    public ClaimRestController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    public ResponseEntity<Claim> submitClaim(@Valid @RequestBody ClaimSubmissionRequest request) {
        Claim createdClaim = claimService.submitClaim(request);
        return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Claim>> getAllClaims(@RequestParam(required = false) ClaimStatus status) {
        if (status != null) {
            return ResponseEntity.ok(claimService.getClaimsByStatus(status));
        }
        return ResponseEntity.ok(claimService.getAllClaims());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Claim> getClaimById(@PathVariable Long id) {
        return claimService.getClaimById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/track/{claimNumber}")
    public ResponseEntity<Claim> trackClaim(@PathVariable String claimNumber) {
        return claimService.getClaimByClaimNumber(claimNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<Claim> reviewClaim(@PathVariable Long id, @Valid @RequestBody ReviewActionRequest request) {
        try {
            Claim updated = claimService.reviewClaim(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", claimService.getTotalCount());
        stats.put("submitted", claimService.getCountByStatus(ClaimStatus.SUBMITTED));
        stats.put("underReview", claimService.getCountByStatus(ClaimStatus.UNDER_REVIEW));
        stats.put("approved", claimService.getCountByStatus(ClaimStatus.APPROVED));
        stats.put("rejected", claimService.getCountByStatus(ClaimStatus.REJECTED));
        return ResponseEntity.ok(stats);
    }
}
