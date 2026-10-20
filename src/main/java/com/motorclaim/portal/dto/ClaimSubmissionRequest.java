package com.motorclaim.portal.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Claim Submission with strict input validation.
 */
public class ClaimSubmissionRequest {

    @NotBlank(message = "Policy number is required")
    @Pattern(regexp = "^[A-Z0-9-]{6,25}$", message = "Policy number must be 6-25 uppercase alphanumeric characters or hyphens")
    private String policyNumber;

    @NotBlank(message = "Claimant name is required")
    @Size(min = 2, max = 100, message = "Claimant name must be between 2 and 100 characters")
    private String claimantName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Please provide a valid email address")
    private String claimantEmail;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+ -]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String claimantPhone;

    @NotBlank(message = "Vehicle registration number is required")
    @Pattern(regexp = "^[A-Z0-9]{5,15}$", message = "Vehicle number must be valid alphanumeric registration (e.g., MH12AB1234)")
    private String vehicleNumber;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate incidentDate;

    @NotBlank(message = "Incident location is required")
    @Size(min = 3, max = 200, message = "Location must be between 3 and 200 characters")
    private String incidentLocation;

    @NotNull(message = "Estimated claim amount is required")
    @DecimalMin(value = "100.00", message = "Estimated amount must be at least ₹100.00")
    @DecimalMax(value = "5000000.00", message = "Estimated amount cannot exceed ₹5,000,000.00")
    private BigDecimal estimatedAmount;

    @NotBlank(message = "Incident description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    public ClaimSubmissionRequest() {
    }

    // Getters and Setters

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber != null ? policyNumber.trim().toUpperCase() : null;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getClaimantEmail() {
        return claimantEmail;
    }

    public void setClaimantEmail(String claimantEmail) {
        this.claimantEmail = claimantEmail;
    }

    public String getClaimantPhone() {
        return claimantPhone;
    }

    public void setClaimantPhone(String claimantPhone) {
        this.claimantPhone = claimantPhone;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber != null ? vehicleNumber.trim().toUpperCase() : null;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getIncidentLocation() {
        return incidentLocation;
    }

    public void setIncidentLocation(String incidentLocation) {
        this.incidentLocation = incidentLocation;
    }

    public BigDecimal getEstimatedAmount() {
        return estimatedAmount;
    }

    public void setEstimatedAmount(BigDecimal estimatedAmount) {
        this.estimatedAmount = estimatedAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
