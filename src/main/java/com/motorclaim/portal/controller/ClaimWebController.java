package com.motorclaim.portal.controller;

import com.motorclaim.portal.dto.ClaimSubmissionRequest;
import com.motorclaim.portal.dto.ReviewActionRequest;
import com.motorclaim.portal.model.Claim;
import com.motorclaim.portal.model.ClaimStatus;
import com.motorclaim.portal.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Web Controller serving HTML views via Thymeleaf for end-users,
 * reviewers, and Selenium automated testing.
 */
@Controller
public class ClaimWebController {

    private final ClaimService claimService;

    @Autowired
    public ClaimWebController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalClaims", claimService.getTotalCount());
        model.addAttribute("submittedCount", claimService.getCountByStatus(ClaimStatus.SUBMITTED));
        model.addAttribute("approvedCount", claimService.getCountByStatus(ClaimStatus.APPROVED));
        model.addAttribute("rejectedCount", claimService.getCountByStatus(ClaimStatus.REJECTED));
        return "index";
    }

    @GetMapping("/submit")
    public String showSubmissionForm(Model model) {
        if (!model.containsAttribute("claimForm")) {
            model.addAttribute("claimForm", new ClaimSubmissionRequest());
        }
        return "submit-claim";
    }

    @PostMapping("/submit")
    public String processSubmission(@Valid @ModelAttribute("claimForm") ClaimSubmissionRequest claimForm,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            return "submit-claim";
        }

        Claim createdClaim = claimService.submitClaim(claimForm);
        redirectAttributes.addFlashAttribute("successClaim", createdClaim);
        redirectAttributes.addFlashAttribute("message", "Claim submitted successfully!");
        return "redirect:/claim-success";
    }

    @GetMapping("/claim-success")
    public String claimSuccess(Model model) {
        if (!model.containsAttribute("successClaim")) {
            return "redirect:/submit";
        }
        return "claim-success";
    }

    @GetMapping("/track")
    public String trackClaim(@RequestParam(value = "claimNumber", required = false) String claimNumber,
                             Model model) {
        if (claimNumber != null && !claimNumber.trim().isEmpty()) {
            Optional<Claim> claimOpt = claimService.getClaimByClaimNumber(claimNumber.trim());
            if (claimOpt.isPresent()) {
                model.addAttribute("claim", claimOpt.get());
            } else {
                model.addAttribute("notFound", true);
                model.addAttribute("searchNumber", claimNumber);
            }
        }
        return "track-claim";
    }

    @GetMapping("/reviewer")
    public String reviewerDashboard(@RequestParam(value = "status", required = false) ClaimStatus status,
                                    Model model) {
        if (status != null) {
            model.addAttribute("claims", claimService.getClaimsByStatus(status));
            model.addAttribute("currentStatusFilter", status);
        } else {
            model.addAttribute("claims", claimService.getAllClaims());
            model.addAttribute("currentStatusFilter", "ALL");
        }
        model.addAttribute("totalClaims", claimService.getTotalCount());
        model.addAttribute("submittedCount", claimService.getCountByStatus(ClaimStatus.SUBMITTED));
        model.addAttribute("approvedCount", claimService.getCountByStatus(ClaimStatus.APPROVED));
        model.addAttribute("rejectedCount", claimService.getCountByStatus(ClaimStatus.REJECTED));
        return "reviewer-dashboard";
    }

    @PostMapping("/reviewer/action")
    public String processReviewAction(@RequestParam("claimId") Long claimId,
                                      @RequestParam("status") ClaimStatus status,
                                      @RequestParam("comments") String comments,
                                      RedirectAttributes redirectAttributes) {
        ReviewActionRequest request = new ReviewActionRequest(status, comments);
        try {
            claimService.reviewClaim(claimId, request);
            redirectAttributes.addFlashAttribute("actionSuccess", "Claim ID #" + claimId + " marked as " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("actionError", "Error processing claim: " + e.getMessage());
        }
        return "redirect:/reviewer";
    }
}
