pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-token')

        // Update these ports to whatever your services actually use
        ROOM_PORT = '8081'
        BOOKING_PORT = '8083'

        // Update this if you don’t have actuator
        HEALTH_PATH = '/actuator/health'
    }

    options {
        timestamps()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build room-service') {
            steps {
                dir('services/room-service') {
                    sh 'mvn clean verify'
                }
            }
        }

        stage('Build booking-service') {
            steps {
                dir('services/booking-service') {
                    sh 'mvn clean verify'
                }
            }
        }

        stage('Check test reports') {
            steps {
                sh '''
                    echo "Checking surefire reports..."
                    find services -type f -path "*/target/surefire-reports/*.xml" || true
                '''
            }
        }

        stage('Check JaCoCo reports') {
            steps {
                sh '''
                    echo "Checking JaCoCo reports..."
                    find services -type f -path "*/target/site/jacoco/jacoco.xml" || true
                    find services -type f -path "*/target/site/jacoco/index.html" || true
                '''
            }
        }

        stage('SonarQube Analysis - room-service') {
            steps {
                dir('services/room-service') {
                    withSonarQubeEnv('LocalSonar') {
                        sh '''
                          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                            -Dsonar.projectKey=room-service \
                            -Dsonar.projectName=room-service \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.scanner.skipJreProvisioning=true
                        '''
                    }
                }
            }
        }

        stage('SonarQube Analysis - booking-service') {
            steps {
                dir('services/booking-service') {
                    withSonarQubeEnv('LocalSonar') {
                        sh '''
                          mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.5.0.6356:sonar \
                            -Dsonar.projectKey=booking-service \
                            -Dsonar.projectName=booking-service \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.scanner.skipJreProvisioning=true
                        '''
                    }
                }
            }
        }

    }

    post {
        always {
            // test reports
            junit testResults: 'services/**/target/surefire-reports/*.xml', allowEmptyResults: true

            // archive artifacts including jacoco html/xml
            archiveArtifacts artifacts: 'services/**/target/*.jar, services/**/target/surefire-reports/*.xml, services/**/target/site/jacoco/**, *.log', fingerprint: true

            // Publish JaCoCo HTML for room-service
            publishHTML(target: [
                reportDir: 'services/room-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo - room-service',
                keepAll: true,
                alwaysLinkToLastBuild: true
            ])

            // Publish JaCoCo HTML for booking-service
            publishHTML(target: [
                reportDir: 'services/booking-service/target/site/jacoco',
                reportFiles: 'index.html',
                reportName: 'JaCoCo - booking-service',
                keepAll: true,
                alwaysLinkToLastBuild: true
            ])

            // stop services if started
            //sh '''
             // if [ -f room-service.pid ]; then kill $(cat room-service.pid) || true; fi
             // if [ -f booking-service.pid ]; then kill $(cat booking-service.pid) || true; fi
            //'''
        }

        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}