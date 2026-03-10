pipeline {
    agent any

    parameters {
        booleanParam(name: 'KEEP_SERVICES_RUNNING', defaultValue: false, description: 'Keep room + booking services running after pipeline ends (for Postman testing)')
    }

    environment {
        GITHUB_TOKEN = credentials('github-token')

        ROOM_PORT = '8081'
        BOOKING_PORT = '8083'
        HEALTH_PATH = '/actuator/health'

        // Single Sonar project for whole repo
        SONAR_PROJECT_KEY = 'classroom-booking'
        SONAR_PROJECT_NAME = 'classroom-booking'
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

        stage('Start microservices and verify they are running') {
            steps {
                sh '''
                  set -e

                  echo "Starting room-service..."
                  ROOM_JAR=$(ls -1 services/room-service/target/*.jar | head -n 1)
                  nohup java -jar "$ROOM_JAR" > room-service.log 2>&1 &
                  echo $! > room-service.pid

                  echo "Starting booking-service..."
                  BOOKING_JAR=$(ls -1 services/booking-service/target/*.jar | head -n 1)
                  nohup java -jar "$BOOKING_JAR" > booking-service.log 2>&1 &
                  echo $! > booking-service.pid

                  echo "Waiting for services to come up..."
                  for i in $(seq 1 30); do
                    ROOM_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${ROOM_PORT}${HEALTH_PATH}" || true)
                    BOOKING_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${BOOKING_PORT}${HEALTH_PATH}" || true)

                    if [ "$ROOM_OK" = "200" ] && [ "$BOOKING_OK" = "200" ]; then
                      echo "Both services are UP"
                      exit 0
                    fi

                    echo "Not ready yet... room=$ROOM_OK booking=$BOOKING_OK (try $i/30)"
                    sleep 2
                  done

                  echo "Services did not start in time"
                  echo "---- room-service.log ----"
                  tail -n 200 room-service.log || true
                  echo "---- booking-service.log ----"
                  tail -n 200 booking-service.log || true
                  exit 1
                '''
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

            script {
                if (!params.KEEP_SERVICES_RUNNING) {
                    sh '''
                      if [ -f room-service.pid ]; then kill $(cat room-service.pid) || true; fi
                      if [ -f booking-service.pid ]; then kill $(cat booking-service.pid) || true; fi
                    '''
                } else {
                    echo "KEEP_SERVICES_RUNNING=true → services left running for Postman testing"
                }
            }
        }

        success { echo 'Pipeline completed successfully.' }
        failure { echo 'Pipeline failed.' }
    }
}