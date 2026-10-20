package com.motorclaim.portal.selenium;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Automated Selenium WebDriver test suite covering critical user journeys
 * for the Motor Insurance Claim Portal.
 */
@TestMethodOrder(OrderAnnotation.class)
public class ClaimPortalSeleniumTest extends BaseSeleniumTest {

    private static String generatedClaimNumber;

    @Test
    @Order(1)
    @DisplayName("Journey 1: Policyholder submits motor claim and receives unique Claim ID")
    void testClaimSubmissionUserJourney() {
        driver.get(baseUrl + "/submit");
        assertThat(driver.getTitle()).contains("File a Claim");

        // Fill form fields
        driver.findElement(By.id("policyNumber")).sendKeys("POL-778899");
        driver.findElement(By.id("claimantName")).sendKeys("Vikram Malhotra");
        driver.findElement(By.id("claimantEmail")).sendKeys("vikram.malhotra@example.com");
        driver.findElement(By.id("claimantPhone")).sendKeys("9876543210");
        driver.findElement(By.id("vehicleNumber")).sendKeys("KA01MJ4321");

        String pastDate = LocalDate.now().minusDays(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        WebElement dateEl = driver.findElement(By.id("incidentDate"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", dateEl, pastDate);

        driver.findElement(By.id("incidentLocation")).sendKeys("Indiranagar 100ft Road, Bengaluru");
        driver.findElement(By.id("estimatedAmount")).sendKeys("18500.00");
        driver.findElement(By.id("description")).sendKeys("Vehicle scratched along the side panel due to roadside barricade.");

        // Submit form
        WebElement submitBtn = driver.findElement(By.id("submitBtn"));
        submitBtn.click();

        // Verify navigation to claim success page
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.urlContains("/claim-success"));

        WebElement heading = driver.findElement(By.id("successHeading"));
        assertThat(heading.getText()).isEqualTo("Claim Submitted Successfully!");

        WebElement claimNumberEl = driver.findElement(By.id("claimNumberDisplay"));
        generatedClaimNumber = claimNumberEl.getText();
        assertThat(generatedClaimNumber).startsWith("CLM-");
        System.out.println("Selenium Journey 1 passed! Generated Claim Reference: " + generatedClaimNumber);
    }

    @Test
    @Order(2)
    @DisplayName("Journey 2: Policyholder tracks claim status using generated Claim ID")
    void testTrackClaimStatusUserJourney() {
        assertThat(generatedClaimNumber).isNotNull();

        driver.get(baseUrl + "/track");
        assertThat(driver.getTitle()).contains("Track Claim Status");

        WebElement searchInput = driver.findElement(By.id("trackSearchInput"));
        searchInput.clear();
        searchInput.sendKeys(generatedClaimNumber);

        WebElement searchBtn = driver.findElement(By.id("trackSearchBtn"));
        searchBtn.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("claimResultCard")));

        WebElement statusBadge = driver.findElement(By.id("trackStatusBadge"));
        assertThat(statusBadge.getText()).isEqualTo("SUBMITTED");

        WebElement vehicleEl = driver.findElement(By.id("trackVehicleNumber"));
        assertThat(vehicleEl.getText()).isEqualTo("KA01MJ4321");

        System.out.println("Selenium Journey 2 passed! Claim status successfully tracked: SUBMITTED");
    }

    @Test
    @Order(3)
    @DisplayName("Journey 3: Reviewer inspects claim, adds remarks, and approves claim")
    void testReviewerApprovalUserJourney() {
        driver.get(baseUrl + "/reviewer");
        assertThat(driver.getTitle()).contains("Reviewer Workbench");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("claimsTable")));

        // Locate review button on dashboard
        WebElement reviewBtn = driver.findElement(By.cssSelector("button.action-btn"));
        reviewBtn.click();

        // Wait for modal to open
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviewModal")));

        // Select APPROVED decision
        WebElement selectElement = driver.findElement(By.id("statusSelect"));
        Select statusSelect = new Select(selectElement);
        statusSelect.selectByValue("APPROVED");

        // Enter surveyor remarks
        WebElement commentsInput = driver.findElement(By.id("commentsInput"));
        commentsInput.clear();
        commentsInput.sendKeys("Surveyor inspected the physical damage. Approved for cashless repair.");

        // Submit review decision
        WebElement submitReviewBtn = driver.findElement(By.id("submitReviewBtn"));
        submitReviewBtn.click();

        // Verify success alert banner
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("actionSuccessAlert")));
        WebElement alert = driver.findElement(By.id("actionSuccessAlert"));
        assertThat(alert.getText()).contains("APPROVED");

        System.out.println("Selenium Journey 3 passed! Claim review and approval complete.");
    }

    @Test
    @Order(4)
    @DisplayName("Journey 4: Form validation catches missing required fields and shows errors")
    void testFormValidationNegativeJourney() {
        driver.get(baseUrl + "/submit");

        // Submit empty form without entering data
        WebElement submitBtn = driver.findElement(By.id("submitBtn"));
        submitBtn.click();

        // Verify error alert or validation errors are displayed
        WebElement errorAlert = driver.findElement(By.id("formErrorAlert"));
        assertThat(errorAlert.isDisplayed()).isTrue();
        assertThat(errorAlert.getText()).contains("Validation Error");

        System.out.println("Selenium Journey 4 passed! Form validation errors triggered as expected.");
    }
}
