package com.motorclaim.portal.dto;

import com.motorclaim.portal.model.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for reviewer decision action (Approve/Reject) on a claim.
 */
public class ReviewActionRequest {

    @NotNull(message = "Review action status is required (APPROVED or REJECTED)")
    private ClaimStatus status;

    @NotBlank(message = "Reviewer comments are required")
    @Size(min = 5, max = 1000, message = "Comments must be between 5 and 1000 characters")
    private String reviewerComments;

    public ReviewActionRequest() {
    }

    public ReviewActionRequest(ClaimStatus status, String reviewerComments) {
        this.status = status;
        this.reviewerComments = reviewerComments;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public String getReviewerComments() {
        return reviewerComments;
    }

    public void setReviewerComments(String reviewerComments) {
        this.reviewerComments = reviewerComments;
    }
}
