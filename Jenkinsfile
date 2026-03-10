pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-token')
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
                    find services -type d -name "surefire-reports" || true
                    find services -type f -path "*/target/surefire-reports/*.xml" || true
                '''
            }
        }

        stage('Check JaCoCo reports') {
            steps {
                sh '''
                    echo "Checking JaCoCo reports..."
                    find services -type d -path "*/target/site/jacoco" || true
                    find services -type f -path "*/target/site/jacoco/*" || true
                '''
            }
        }

        stage('SonarQube Analysis') {
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

        stage('Quality Gate') {
            steps {
                timeout(time: 2, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }


    }

    post {
        always {
            junit testResults: 'services/**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'services/**/target/*.jar, services/**/target/surefire-reports/*.xml, services/**/target/site/jacoco/**', fingerprint: true
            
            publishHTML(target: [
            reportDir: 'services/room-service/target/site/jacoco',
            reportFiles: 'index.html',
            reportName: 'JaCoCo Code Coverage',
            keepAll: true,
            alwaysLinkToLastBuild: true
        ])

        }

        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}