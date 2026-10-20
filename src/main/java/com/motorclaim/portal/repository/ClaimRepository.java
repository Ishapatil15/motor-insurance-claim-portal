package com.motorclaim.portal.repository;

import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Claim entity.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByPolicyNumber(String policyNumber);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findAllByOrderByCreatedAtDesc();

    long countByStatus(ClaimStatus status);
}
