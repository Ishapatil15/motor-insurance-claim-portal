# CI/CD Pipeline for a Motor Insurance Claim Portal

[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square&logo=jenkins)](https://jenkins.io)
[![Java Version](https://img.shields.io/badge/Java-21%20LTS-orange?style=flat-square&logo=openjdk)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Selenium](https://img.shields.io/badge/E2E%20Tests-Selenium%20WebDriver%204-green?style=flat-square&logo=selenium)](https://selenium.dev)
[![Docker](https://img.shields.io/badge/Container-Docker-blue?style=flat-square&logo=docker)](https://docker.com)
[![Ansible](https://img.shields.io/badge/IaC-Ansible-red?style=flat-square&logo=ansible)](https://ansible.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

---

## 📌 Project Overview
The **Motor Insurance Claim Portal** is an end-to-end cloud-native enterprise application engineered to automate the intake, validation, surveyor review, approval/rejection, and real-time tracking of vehicular accident insurance claims. 

Integrated with a robust **DevOps CI/CD Pipeline**, the portal automates every phase of the software delivery lifecycle from Git commit and automated Selenium testing to Docker multi-stage containerization, registry distribution, and Ansible server provisioning with automated rollback capabilities.

---

## 🚀 Key Functional Capabilities
1. **Self-Service Claim Submission**: Responsive web form with instant client- and server-side validation for policy numbers, vehicle registration, past incident dates, and repair estimates.
2. **Instant Claim Reference Generation**: Predictable, human-readable reference IDs (e.g. `CLM-2026-XXXXX`) generated upon intake.
3. **Surveyor Reviewer Workbench**: Real-time adjudication dashboard allowing claim adjusters to filter claims by status, review accident details, and issue approvals or rejections with mandatory remarks.
4. **24/7 Claim Tracking**: Policyholders can track their claim journey via a visual progression stepper (`Submitted` ➔ `Under Review` ➔ `Decision Made`).
5. **Production Health Actuator**: Native Spring Boot Actuator endpoints (`/actuator/health`) for automated CI/CD and container health checks.

---

## 🛠️ Technology Stack & DevOps Toolchain

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Runtime & SDK** | Java 21 LTS | High-performance execution runtime |
| **Backend Framework** | Spring Boot 3.3.4 | REST APIs, Spring MVC, Spring Data JPA |
| **Presentation Tier** | Thymeleaf + Bootstrap 5 | Dynamic SSR UI with responsive styling |
| **Database** | H2 Database / MySQL | In-memory / persistent relational storage |
| **Build Automation** | Apache Maven 3.9.16 | Compilation, dependency resolution, packaging |
| **Version Control** | Git & GitHub | Distributed version control & GitFlow branching |
| **Continuous Integration** | Jenkins LTS | Automated build trigger, surefire test reporting, artifact archiving |
| **Pipeline as Code** | Jenkinsfile (Declarative) | Multi-stage pipeline with parameterized deployment |
| **Test Automation** | Selenium WebDriver 4 + JUnit 5 | Headless Chrome E2E user journey automation & screenshot capture |
| **Containerization** | Docker & Docker Compose | Multi-stage build, minimal JRE runtime, non-root user |
| **Configuration Mgmt** | Ansible & Puppet | Target node provisioning, idempotency, health check, rollback |

---

## 📂 Project Directory Structure

```
d:\DevOps\
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md
│   │   └── feature_request.md
│   └── PULL_REQUEST_TEMPLATE.md
├── docs/
│   ├── task-01-problem-definition-and-scope.md
│   ├── task-02-agile-planning-and-devops-workflow.md
│   ├── task-03-requirements-architecture-technology-setup.md
│   ├── task-04-git-github-repository-initialization.md
│   ├── task-05-feature-development-with-branching.md
│   ├── task-06-mvp-completion-and-git-collaboration.md
│   ├── task-07-jenkins-installation-and-ci-job.md
│   ├── task-08-pipeline-as-code-and-server-deployment.md
│   ├── task-09-selenium-test-design-and-local-execution.md
│   ├── task-10-continuous-testing-in-jenkins.md
│   ├── task-11-docker-image-and-container-lifecycle.md
│   ├── task-12-jenkins-docker-continuous-deployment.md
│   ├── task-13-configuration-management-script.md
│   ├── task-14-automated-provisioning-and-reliability-validation.md
│   └── branching-policy.md
├── src/
│   ├── main/
│   │   ├── java/com/motorclaim/portal/...
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── templates/
│   │       └── static/
│   └── test/
│       ├── java/com/motorclaim/portal/unit/...
│       └── java/com/motorclaim/portal/selenium/...
├── ansible/
│   ├── inventory/hosts.ini
│   ├── playbooks/
│   │   ├── site.yml
│   │   ├── deploy.yml
│   │   └── rollback.yml
│   └── roles/claim_portal/...
├── puppet/manifests/site.pp
├── scripts/
│   ├── docker-lifecycle.ps1
│   ├── docker-lifecycle.sh
│   └── health_check.py
├── Dockerfile
├── docker-compose.yml
├── Jenkinsfile
├── pom.xml
└── README.md
```

---

## ⚡ Quick Start Guide

### 1. Build and Run Locally
```bash
# Compile and run all unit & Selenium tests
mvn test

# Package standalone executable JAR
mvn package -DskipTests

# Run the application
java -jar target/claim-portal.jar
```

### 2. Access the Application
- **Home**: [http://localhost:8080/](http://localhost:8080/)
- **File a Claim**: [http://localhost:8080/submit](http://localhost:8080/submit)
- **Track Claim**: [http://localhost:8080/track](http://localhost:8080/track)
- **Reviewer Workbench**: [http://localhost:8080/reviewer](http://localhost:8080/reviewer)
- **Actuator Health Probe**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:claimdb`)

---

## 🧪 Testing Strategy
- **Unit & REST Controller Tests**: Mockito & MockMvc tests (`mvn test -Dtest=ClaimServiceTest,ClaimRestControllerTest`).
- **Selenium E2E Tests**: Headless Chrome tests covering 4 user journeys: claim filing, status tracking, reviewer adjudication, and negative validation (`mvn test -Dtest=ClaimPortalSeleniumTest`).
- **Failure Screenshot Capture**: Any test failure automatically captures a full-page PNG screenshot in `target/screenshots/`.

---

## 🚢 CI/CD & Deployment
- **Jenkins CI**: Automated build trigger, JUnit report publishing, quality gating.
- **Jenkinsfile**: Pipeline-as-Code with Checkout ➔ Build ➔ Test ➔ Package ➔ Containerize ➔ Deploy.
- **Docker**: Multi-stage build yielding a minimal, secure container runtime image running as non-root user.
- **Ansible**: Complete configuration management, idempotency demonstration, automated health-checking, and zero-downtime rollback.

---
**Author**: DevOps Engineering Team  
**License**: MIT License
