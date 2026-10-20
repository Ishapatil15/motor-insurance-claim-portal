# Task 5: Feature Development with Branching
## Motor Insurance Claim Portal

---

### 1. Objective & Scope
Task 5 demonstrates disciplined Git feature-branch workflows:
1. Creating a dedicated feature branch `feature/claim-submission` from `develop`.
2. Implementing the core Claim Submission workflow (Domain entity, JPA repository, DTO validation, service layer, web controller, and Thymeleaf templates).
3. Utilizing Git lifecycle commands: `git add`, `git commit`, `git status`, `git log`.
4. Simulating a formal Pull Request (PR #1) with peer code review comments and sign-off.
5. Executing a non-fast-forward merge (`git merge --no-ff`) into `develop` to maintain clear history.

---

### 2. Git Workflow Commands Executed

```bash
# Step 1: Create and switch to develop branch
git checkout -b develop

# Step 2: Create feature branch for claim submission
git checkout -b feature/claim-submission

# Step 3: Implement domain, controller, templates and tests
# Stage all changes
git add src/

# Step 4: Commit with Conventional Commit message
git commit -m "feat(claim): implement core motor insurance claim submission workflow and validation"

# Step 5: Switch back to develop
git checkout develop

# Step 6: Perform non-fast-forward merge
git merge --no-ff feature/claim-submission -m "merge: PR #1 - Implement core motor insurance claim submission workflow"

# Step 7: Inspect commit graph
git log --graph --oneline -n 6
```

---

### 3. Pull Request Details (PR #1)

- **PR Title**: `[FEAT] Implement Core Motor Insurance Claim Submission Workflow`
- **Source Branch**: `feature/claim-submission`
- **Target Branch**: `develop`
- **Author**: `Ishapatil15`
- **Reviewer**: Lead DevOps / Senior Software Architect

#### PR Description:
> Implements the primary user workflow for the Motor Insurance Claim Portal:
> - Created `Claim` JPA entity with fields for policy number, vehicle registration, incident date, location, amount, and claimant contact details.
> - Built `ClaimSubmissionRequest` DTO with strict input validation annotations (`@NotBlank`, `@Pattern`, `@PastOrPresent`, `@DecimalMin`, `@DateTimeFormat`).
> - Implemented `ClaimServiceImpl` with unique claim reference generator (`CLM-YYYY-XXXX`).
> - Developed responsive Thymeleaf UI templates (`submit-claim.html`, `claim-success.html`).

#### Peer Review Comments & Resolution:
1. **Reviewer Comment (Architect)**:  
   *“Ensure the `incidentDate` field in `ClaimSubmissionRequest` specifies `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` so that standard browser date-pickers bind without conversion errors across different system locales.”*  
   **Resolution (Author)**:  
   *“Added `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` to `incidentDate`. Verified date binding and unit tests.”*
2. **Reviewer Comment (QA Lead)**:  
   *“Verify that estimated amount enforces minimum ₹100 and maximum ₹5,000,000 bounds.”*  
   **Resolution (Author)**:  
   *“Validated with `@DecimalMin(value = "100.00")` and `@DecimalMax(value = "5000000.00")`. Unit and mock MVC tests pass cleanly.”*

#### PR Sign-off:
- **Status**: **APPROVED & MERGED**
- **Merge Strategy**: `--no-ff` (Preserve branch topology)

---

### 4. Git Graph & Merge Evidence

```
*   25298a9 (HEAD -> develop) merge: PR #1 - Implement core motor insurance claim submission workflow
|\  
| * 4947d8d (feature/claim-submission) feat(claim): implement core motor insurance claim submission workflow and validation
|/  
* 80bdf6c (main) chore: initialize motor insurance claim portal skeleton
```

---
**Status**: COMPLETE & VERIFIED
