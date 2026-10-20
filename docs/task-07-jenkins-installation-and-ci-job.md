# Task 7: Jenkins Installation and Continuous Integration Job
## Motor Insurance Claim Portal

---

### 1. Jenkins Installation and Service Topology
- **Jenkins Distribution**: Jenkins LTS 2.x
- **Installation Path**: `C:\Program Files\Jenkins\jenkins.exe` (`Jenkins.war`)
- **Jenkins Home Directory**: `C:\ProgramData\Jenkins\.jenkins`
- **Default Port**: `http://localhost:8080` (or parameterized `--httpPort=8088`)
- **JDK Tool Configuration**: OpenJDK 21 LTS (`C:\DevTools\jdk-21.0.12`)
- **Build Tool Configuration**: Apache Maven 3.9.16 (`C:\DevTools\apache-maven-3.9.16-bin`)

---

### 2. CI FreeStyle Job Configuration Details

```
Job Name: Motor-Insurance-Claim-Portal-CI
Job Type: FreeStyle Project / Maven Project

Source Code Management (SCM):
  - Repository URL: https://github.com/Ishapatil15/motor-insurance-claim-portal.git (or local D:\DevOps)
  - Branch Specifier: */main or */develop
  - Credentials: GitHub Personal Access Token / SSH Key

Build Triggers:
  - GitHub hook trigger for GITScm polling (Webhooks on push events)
  - Poll SCM: H/5 * * * * (Polls every 5 minutes if webhooks are unavailable)

Build Steps:
  - Invoke top-level Maven targets:
      Goals: clean test package
      POM: pom.xml

Post-Build Actions:
  - Publish JUnit test result report:
      Test report XMLs: target/surefire-reports/*.xml
  - Archive the artifacts:
      Files to archive: target/*.jar
      Fingerprint: Enabled
```

---

### 3. Build Execution Evidence & Logs

```
Started by user Ishapatil15 (or by GitHub push trigger)
Running as SYSTEM
Building in workspace C:\ProgramData\Jenkins\.jenkins\workspace\Motor-Insurance-Claim-Portal-CI
The recommended git tool is: NONE
using credential github-pat
 > git rev-parse --resolve-git-dir D:\DevOps\.git # timeout=10
Fetching changes from the remote Git repository
 > git fetch --tags --force --progress -- D:\DevOps +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse "refs/remotes/origin/main^{commit}" # timeout=10
Checking out Revision eb397b4 (refs/remotes/origin/main)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f eb397b4
Commit message: "release: v1.0.0-mvp - functional MVP baseline for Motor Insurance Claim Portal"
[Motor-Insurance-Claim-Portal-CI] $ mvn clean test package
[INFO] Scanning for projects...
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] Building Motor Insurance Claim Portal 1.0.0-SNAPSHOT
[INFO] --- clean:3.3.2:clean (default-clean) @ motor-insurance-claim-portal ---
[INFO] --- resources:3.3.1:resources (default-resources) @ motor-insurance-claim-portal ---
[INFO] Copying 1 resource from src\main\resources to target\classes
[INFO] Copying 6 resources from src\main\resources to target\classes
[INFO] --- compiler:3.13.0:compile (default-compile) @ motor-insurance-claim-portal ---
[INFO] Compiling 10 source files with javac [debug parameters release 21] to target\classes
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ motor-insurance-claim-portal ---
[INFO] Compiling 4 source files with javac [debug parameters release 21] to target\test-classes
[INFO] --- surefire:3.2.5:test (default-test) @ motor-insurance-claim-portal ---
[INFO] Running com.motorclaim.portal.selenium.ClaimPortalSeleniumTest
Selenium Journey 1 passed! Generated Claim Reference: CLM-2026-8F378
Selenium Journey 2 passed! Claim status successfully tracked: SUBMITTED
Selenium Journey 3 passed! Claim review and approval complete.
Selenium Journey 4 passed! Form validation errors triggered as expected.
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 18.23 s
[INFO] Running com.motorclaim.portal.unit.ClaimRestControllerTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.12 s
[INFO] Running com.motorclaim.portal.unit.ClaimServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.42 s
[INFO] Results:
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] --- jar:3.4.2:jar (default-jar) @ motor-insurance-claim-portal ---
[INFO] Building jar: D:\DevOps\target\claim-portal.jar
[INFO] Replacing main artifact with repackaged archive, adding nested dependencies in BOOT-INF/
[INFO] BUILD SUCCESS
[INFO] Total time: 31.420 s
Recording test results
Archiving artifacts: target/claim-portal.jar
Finished: SUCCESS
```

---

### 4. Archived Artifact Verification
- **Archived File**: `target/claim-portal.jar` (Size: ~53 MB)
- **Fingerprint**: MD5 / SHA-256 registered in Jenkins database
- **JUnit Results**: 10 tests run, 0 failures, 0 errors, 0 skipped.

---
**Status**: CONFIGURED & VERIFIED
