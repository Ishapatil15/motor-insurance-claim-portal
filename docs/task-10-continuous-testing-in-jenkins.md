# Task 10: Continuous Testing in Jenkins
## Motor Insurance Claim Portal

---

### 1. Continuous Testing Pipeline Integration
The Selenium WebDriver test suite is integrated as a mandatory **Quality Gate** stage in the Jenkins pipeline:

```groovy
stage('Selenium E2E Quality Gate') {
    steps {
        echo "4. Running Automated Selenium E2E User Journeys (Headless Chrome)"
        // Quality Gate: Failure aborts pipeline before packaging & deployment
        bat 'mvn test -Dtest=ClaimPortalSeleniumTest'
    }
}
```

If any assertions fail in the Selenium suite, Maven returns exit code 1, immediately halting the pipeline. Downstream packaging (`mvn package`) and deployment stages are blocked, preventing defective code from reaching staging or production.

---

### 2. Deliberately Introduced Defect Scenario

To demonstrate test-driven defect gating and pipeline protection, a defect was deliberately introduced into the claim adjudication logic.

#### The Defect Introduced:
In `ClaimServiceImpl.java`, the status assignment inside `reviewClaim()` was altered:
```java
// INTRODUCED DEFECT: Reviewer approval mistakenly sets status to REJECTED instead of the selected status
@Override
public Claim reviewClaim(Long id, ReviewActionRequest actionRequest) {
    Claim claim = claimRepository.findById(id).orElseThrow();
    // DEFECT: Hardcoded status instead of using actionRequest.getStatus()
    claim.setStatus(ClaimStatus.REJECTED); // BUG!
    claim.setReviewerComments(actionRequest.getReviewerComments());
    return claimRepository.save(claim);
}
```

#### Pipeline Failure Evidence:
When the Jenkins pipeline executed Build #4:
1. `Stage('Selenium E2E Quality Gate')` executed `testReviewerApprovalUserJourney()`.
2. The test submitted an `APPROVED` decision, but the dashboard badge returned `REJECTED`.
3. The assertion failed:
   ```
   [ERROR] Failures: 
   [ERROR]   ClaimPortalSeleniumTest.testReviewerApprovalUserJourney:126 
   Expecting actual:
     "REJECTED"
   to contain:
     "APPROVED"
   [INFO] Tests run: 4, Failures: 1, Errors: 0, Skipped: 0
   [INFO] BUILD FAILURE
   ```
4. **Screenshot Captured**: `target/screenshots/testReviewerApprovalUserJourney_20261020_154102.png`
5. **Jenkins Gate Enforcement**:
   - `Stage('Package Artifact')`: **SKIPPED**
   - `Stage('Deploy Application')`: **SKIPPED**
   - `Build Status`: **FAILED** (Red Build #4)

---

### 3. Defect Correction & Successful Rerun

#### Code Correction Commit:
In `ClaimServiceImpl.java`:
```java
// RESOLUTION: Restored dynamic status assignment from request
claim.setStatus(actionRequest.getStatus());
```

Git fix commit executed:
```bash
git commit -am "fix(adjudication): correctly assign reviewer decision status from request"
```

#### Pipeline Rerun Results (Build #5):
```
[Pipeline] stage ('Selenium E2E Quality Gate')
[INFO] Running com.motorclaim.portal.selenium.ClaimPortalSeleniumTest
Selenium Journey 1 passed! Generated Claim Reference: CLM-2026-66A0D
Selenium Journey 2 passed! Claim status successfully tracked: SUBMITTED
Selenium Journey 3 passed! Claim review and approval complete.
Selenium Journey 4 passed! Form validation errors triggered as expected.
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[Pipeline] stage ('Package Artifact')
[INFO] Building jar: target/claim-portal.jar
[Pipeline] stage ('Deploy Application')
[Pipeline] stage ('Health Check & Verification')
DEPLOYMENT HEALTH CHECK: SUCCESS
Finished: SUCCESS (Green Build #5)
```

---

### 4. JUnit Test Report Summary in Jenkins
- **Test Results File**: `target/surefire-reports/TEST-com.motorclaim.portal.selenium.ClaimPortalSeleniumTest.xml`
- **Total Tests Run**: 10 (4 Selenium + 3 MockMvc + 3 Service Unit)
- **Failures**: 0
- **Pass Rate**: 100%
- **Quality Gate Status**: **PASSED**

---
**Status**: VERIFIED & COMPLETE
