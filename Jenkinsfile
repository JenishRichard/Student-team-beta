pipeline {
    agent any

    environment {
        GITHUB_TOKEN = credentials('github-token')

        EMAIL_RECIPIENTS = 'sanket.shetty9423@gmail.com'

        DB_USER = credentials('rds-db-user')
        DB_PASSWORD = credentials('rds-db-password')

        DOCKER_REPO_ROOM = 'sanketshetty23/room-service'
        DOCKER_REPO_BOOKING = 'sanketshetty23/booking-service'
    }

    options { timestamps() }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        // Single Build stage (build both services)
        stage('Build & Test room service') {
            steps {
                sh '''
                  set -e
                  mvn -f services/room-service/pom.xml clean verify
                '''
            }
        }

        stage('Build & Test booking service') {
            steps {
                sh '''
                  set -e
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
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('LocalSonar') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
                        -N \
                        -Dsonar.projectKey=classroom-booking \
                        -Dsonar.projectName=classroom-booking \
                        -Dsonar.sources=services/room-service/src/main/java,services/booking-service/src/main/java \
                        -Dsonar.tests=services/room-service/src/test/java,services/booking-service/src/test/java \
                        -Dsonar.java.binaries=services/room-service/target/classes,services/booking-service/target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=services/room-service/target/site/jacoco/jacoco.xml,services/booking-service/target/site/jacoco/jacoco.xml \
                        -Dsonar.login=$SONAR_TOKEN
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

        stage('Run Karate Tests') {
            steps {
                dir('services/karate-tests') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                docker build -t ${DOCKER_REPO_ROOM}:${BUILD_NUMBER} -t ${DOCKER_REPO_ROOM}:latest services/room-service
                docker build -t ${DOCKER_REPO_BOOKING}:${BUILD_NUMBER} -t ${DOCKER_REPO_BOOKING}:latest services/booking-service
                """
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    docker push ${DOCKER_REPO_ROOM}:${BUILD_NUMBER}
                    docker push ${DOCKER_REPO_ROOM}:latest
                    docker push ${DOCKER_REPO_BOOKING}:${BUILD_NUMBER}
                    docker push ${DOCKER_REPO_BOOKING}:latest
                    '''
                }
            }
        }

    }

    post {
        always {
            junit testResults: 'services/**/target/surefire-reports/*.xml, services/karate-tests/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'services/**/target/*.jar, services/**/target/surefire-reports/*.xml, services/**/target/site/jacoco/**, services/karate-tests/target/karate-reports/**, services/karate-tests/target/surefire-reports/*.xml, *.log', fingerprint: true

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

            publishHTML(target: [
                reportDir: 'services/karate-tests/target/karate-reports',
                reportFiles: 'karate-summary.html',
                reportName: 'Karate API Test Report',
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
            to: "${env.EMAIL_RECIPIENTS}"
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
                to: "${env.EMAIL_RECIPIENTS}"
            )
        }
    }
}