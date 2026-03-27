pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-token')

        // Single Sonar project for whole repo
        SONAR_PROJECT_KEY = 'classroom-booking'
        SONAR_PROJECT_NAME = 'classroom-booking'
        EMAIL_RECIPIENTS = 'sanket.shetty9423@gmail.com'
    }

    options { timestamps() }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        // Single Build stage (build both services)
        stage('Build & Test (room + booking)') {
            steps {
                sh '''
                  set -e
                  mvn -f services/room-service/pom.xml clean verify
                  mvn -f services/booking-service/pom.xml clean verify
                '''
            }
        }

        stage('Check reports') {
            steps {
                sh '''
                    echo "Surefire reports:"
                    find services -type f -path "*/target/surefire-reports/*.xml" || true

                    echo "JaCoCo XML:"
                    find services -type f -path "*/target/site/jacoco/jacoco.xml" || true

                    echo "JaCoCo HTML:"
                    find services -type f -path "*/target/site/jacoco/index.html" || true
                '''
            }
        }

        // Single SonarQube analysis for both services into ONE Sonar project
        stage('SonarQube Analysis (single project)') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    script {
                        def scannerHome = tool 'LocalSonarScanner'
                        sh """
                        ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=classroom-booking \
                            -Dsonar.projectName=classroom-booking \
                            -Dsonar.sources=services/room-service/src/main,services/booking-service/src/main \
                            -Dsonar.tests=services/room-service/src/test,services/booking-service/src/test \
                            -Dsonar.java.binaries=services/room-service/target/classes,services/booking-service/target/classes \
                            -Dsonar.junit.reportPaths=services/room-service/target/surefire-reports,services/booking-service/target/surefire-reports \
                            -Dsonar.coverage.jacoco.xmlReportPaths=services/room-service/target/site/jacoco/jacoco.xml,services/booking-service/target/site/jacoco/jacoco.xml \
                            -Dsonar.scanner.skipJreProvisioning=true
                        """
                    }
                }
            }
        }

    }

    post {
        always {
            junit testResults: 'services/**/target/surefire-reports/*.xml', allowEmptyResults: true

            archiveArtifacts artifacts: 'services/**/target/*.jar, services/**/target/surefire-reports/*.xml, services/**/target/site/jacoco/**, *.log', fingerprint: true

            publishHTML(target: [
                reportDir: 'services/room-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo - room-service',
                keepAll: true,
                alwaysLinkToLastBuild: true
            ])

            publishHTML(target: [
                reportDir: 'services/booking-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo - booking-service',
                keepAll: true,
                alwaysLinkToLastBuild: true
            ])

       
        }

        success {
                echo 'Pipeline completed successfully.'
        }

        unstable {
            emailext(
            subject: "UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            body: """Build is UNSTABLE

            Job: ${env.JOB_NAME}
            Build Number: ${env.BUILD_NUMBER}
            Check console output: ${env.BUILD_URL}
            """,
            to: ${env.EMAIL_RECIPIENTS}
            )
        }

        failure {
            echo 'Pipeline failed.'
            
            emailext(
                subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """Build FAILED

                Job: ${env.JOB_NAME}
                Build Number: ${env.BUILD_NUMBER}
                Check console output: ${env.BUILD_URL}
                """,
                to: ${env.EMAIL_RECIPIENTS}
            )
        }
    }
}