pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'staging', 'prod'],
            description: 'Target deployment environment'
        )
        string(
            name: 'SERVER_PORT',
            defaultValue: '8085',
            description: 'HTTP Port for deploying the Claim Portal application'
        )
        choice(
            name: 'DEPLOY_TARGET',
            choices: ['embedded-tomcat', 'docker-container', 'standalone-server'],
            description: 'Target deployment runtime architecture'
        )
    }

    environment {
        APP_NAME     = 'motor-insurance-claim-portal'
        ARTIFACT_JAR = 'target/claim-portal.jar'
        DOCKER_IMAGE = "motorclaim/portal:${BUILD_NUMBER}"
        LATEST_IMAGE = "motorclaim/portal:latest"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo "=========================================="
                echo "1. Checking out Motor Insurance Claim Portal from Git SCM"
                echo "Commit SHA: ${GIT_COMMIT ?: 'local'}"
                echo "Target Environment: ${params.ENVIRONMENT}"
                echo "=========================================="
                checkout scm
            }
        }

        stage('Compile & Static Verification') {
            steps {
                echo "2. Compiling Java 21 sources with Apache Maven"
                bat 'mvn clean compile'
            }
        }

        stage('Unit & Controller Tests') {
            steps {
                echo "3. Executing Unit & MockMvc Controller Tests"
                bat 'mvn test -Dtest=ClaimServiceTest,ClaimRestControllerTest'
            }
        }

        stage('Selenium E2E Quality Gate') {
            steps {
                echo "4. Running Automated Selenium E2E User Journeys (Headless Chrome)"
                // Quality Gate: Failure aborts pipeline before packaging & deployment
                bat 'mvn test -Dtest=ClaimPortalSeleniumTest'
            }
        }

        stage('Package Artifact') {
            steps {
                echo "5. Packaging Executable Spring Boot JAR"
                bat 'mvn package -DskipTests'
            }
        }

        stage('Publish Reports & Archive Artifact') {
            steps {
                echo "6. Archiving Build Artifacts & Publishing Test Results"
                junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Build Docker Image') {
            when {
                expression { return params.DEPLOY_TARGET == 'docker-container' }
            }
            steps {
                echo "7. Building Multi-Stage Docker Image: ${env.DOCKER_IMAGE}"
                bat """
                    docker build -t ${env.DOCKER_IMAGE} -t ${env.LATEST_IMAGE} .
                    docker images | findstr "motorclaim"
                """
            }
        }

        stage('Deploy Application') {
            steps {
                echo "8. Deploying Claim Portal to target environment: ${params.ENVIRONMENT} on port ${params.SERVER_PORT}"
                script {
                    if (params.DEPLOY_TARGET == 'docker-container') {
                        bat """
                            docker stop claim-portal-container || echo No active container
                            docker rm claim-portal-container || echo Container clean
                            docker run -d --name claim-portal-container -p ${params.SERVER_PORT}:8080 -e SERVER_PORT=8080 ${env.DOCKER_IMAGE}
                        """
                    } else {
                        // Embedded Tomcat Deployment
                        echo "Starting embedded Tomcat service in background..."
                        bat """
                            powershell -Command "Get-Process -Name java -ErrorAction SilentlyContinue | Where-Object { \$_.CommandLine -like '*${params.SERVER_PORT}*' } | Stop-Process -Force"
                            start "MotorClaimPortal-${params.ENVIRONMENT}" /B java -jar ${env.ARTIFACT_JAR} --server.port=${params.SERVER_PORT}
                        """
                    }
                }
            }
        }

        stage('Health Check & Verification') {
            steps {
                echo "9. Verifying application health probe on http://localhost:${params.SERVER_PORT}/actuator/health"
                bat """
                    powershell -Command "Start-Sleep -Seconds 8; try { \$res = Invoke-RestMethod -Uri http://localhost:${params.SERVER_PORT}/actuator/health -TimeoutSec 10; Write-Host 'Health Probe Response:' (\$res | ConvertTo-Json); if (\$res.status -eq 'UP') { Write-Host 'DEPLOYMENT HEALTH CHECK: SUCCESS' } else { exit 1 } } catch { Write-Host 'Health Check Failed:' \$_; exit 1 }"
                """
            }
        }
    }

    post {
        always {
            echo "Pipeline run completed for build #${env.BUILD_NUMBER}"
        }
        success {
            echo "=========================================================="
            echo "SUCCESS: Motor Insurance Claim Portal deployed successfully!"
            echo "Portal URL: http://localhost:${params.SERVER_PORT}/"
            echo "Reviewer Portal: http://localhost:${params.SERVER_PORT}/reviewer"
            echo "Health Probe: http://localhost:${params.SERVER_PORT}/actuator/health"
            echo "=========================================================="
        }
        failure {
            echo "=========================================================="
            echo "FAILED: CI/CD Pipeline encountered errors. Deployment aborted."
            echo "Quality Gate triggered. Check test reports & screenshots."
            echo "=========================================================="
        }
    }
}
