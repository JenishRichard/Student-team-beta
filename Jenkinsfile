pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'BUILD_UI', defaultValue: true, description: 'Run UI install/build stages')
    booleanParam(name: 'BUILD_DOCKER', defaultValue: false, description: 'Build Docker images using docker compose')
    booleanParam(name: 'RUN_RDS_INTEGRATION', defaultValue: false, description: 'Run optional RDS-backed startup checks for DB services')
    string(name: 'RDS_HOST', defaultValue: 'classroom-dev-db.cvwy4uckycwn.eu-west-1.rds.amazonaws.com', description: 'RDS hostname used only in optional integration stage')
    string(name: 'RDS_PORT', defaultValue: '3306', description: 'RDS port used only in optional integration stage')
    string(name: 'RDS_CREDENTIALS_ID', defaultValue: 'classroom-rds-admin', description: 'Jenkins Username/Password credentials ID for RDS')
  }

  environment {
    MAVEN_IMAGE = 'maven:3.9.9-eclipse-temurin-21'
    NODE_IMAGE = 'node:20'
    MAVEN_ARGS = '-B -ntp'
    SERVICE_DIRS = 'services/discovery-server services/config-server services/auth-service services/room-service services/booking-service services/api-gateway'
  }

  stages {
    stage('Branch Guard') {
      steps {
        script {
          if ((env.BRANCH_NAME ?: '') && env.BRANCH_NAME != 'development') {
            currentBuild.result = 'NOT_BUILT'
            error("This pipeline is restricted to 'development'. Current branch: ${env.BRANCH_NAME}")
          }
        }
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Validate Toolchain') {
      steps {
        sh '''
          set -eux
          docker --version
          docker run --rm "$MAVEN_IMAGE" java -version
          docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace "$MAVEN_IMAGE" mvn -version
          if [ "${BUILD_UI}" = "true" ]; then
            docker run --rm "$NODE_IMAGE" node -v
            docker run --rm "$NODE_IMAGE" npm -v
          fi
        '''
      }
    }

    stage('Build & Test Backend Services') {
      steps {
        sh '''
          set -eux
          for svc in $SERVICE_DIRS; do
            echo "==> Testing $svc"
            docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace "$MAVEN_IMAGE" \
              mvn $MAVEN_ARGS -f "$svc/pom.xml" clean test
          done
        '''
      }
    }

    stage('Package Backend Artifacts') {
      steps {
        sh '''
          set -eux
          for svc in $SERVICE_DIRS; do
            echo "==> Packaging $svc"
            docker run --rm -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace "$MAVEN_IMAGE" \
              mvn $MAVEN_ARGS -f "$svc/pom.xml" -DskipTests package
          done
        '''
      }
    }

    stage('Build UI') {
      when {
        expression { return params.BUILD_UI }
      }
      steps {
        sh '''
          set -eux
          docker run --rm -v "$PWD/ui":/workspace -w /workspace "$NODE_IMAGE" sh -lc '
            node -v
            npm -v
            npm ci
            npm run build
          '
        '''
      }
    }

    stage('Build Docker Images') {
      when {
        expression { return params.BUILD_DOCKER }
      }
      agent any
      steps {
        sh '''
          set -eux
          docker compose build
        '''
      }
    }

    stage('RDS Integration (Optional)') {
      when {
        expression { return params.RUN_RDS_INTEGRATION }
      }
      steps {
        withCredentials([
          usernamePassword(
            credentialsId: params.RDS_CREDENTIALS_ID,
            usernameVariable: 'RDS_DB_USER',
            passwordVariable: 'RDS_DB_PASSWORD'
          )
        ]) {
          sh '''
            set -eu
            mkdir -p .runlogs

            start_and_check() {
              svc_dir="$1"
              app_name="$2"
              port="$3"
              service_name="${svc_dir##*/}"
              container_name="ci-rds-${service_name}"

              echo "==> Starting ${svc_dir} on port ${port} with RDS (${RDS_HOST}:${RDS_PORT})"
              docker rm -f "$container_name" >/dev/null 2>&1 || true
              docker run -d --name "$container_name" \
                -e DB_HOST="$RDS_HOST" \
                -e DB_PORT="$RDS_PORT" \
                -e DB_USER="$RDS_DB_USER" \
                -e DB_PASSWORD="$RDS_DB_PASSWORD" \
                -v "$PWD":/workspace \
                -v "$HOME/.m2":/root/.m2 \
                -w /workspace \
                "$MAVEN_IMAGE" \
                mvn $MAVEN_ARGS -f "${svc_dir}/pom.xml" \
                spring-boot:run -Dspring-boot.run.arguments="--server.port=${port}" >/dev/null

              started=0

              for _ in $(seq 1 60); do
                if docker logs "$container_name" 2>&1 | grep -q "Started ${app_name}"; then
                  started=1
                  break
                fi
                if ! docker ps --format '{{.Names}}' | grep -q "^${container_name}$"; then
                  echo "${svc_dir} exited before startup. Last logs:"
                  docker logs --tail 120 "$container_name" || true
                  exit 1
                fi
                sleep 2
              done

              if [ "$started" -ne 1 ]; then
                echo "Timed out waiting for ${svc_dir} startup. Last logs:"
                docker logs --tail 120 "$container_name" || true
                docker rm -f "$container_name" >/dev/null 2>&1 || true
                exit 1
              fi

              echo "${svc_dir} started successfully with RDS."
              docker rm -f "$container_name" >/dev/null 2>&1 || true
            }

            start_and_check "services/auth-service" "AuthServiceApplication" "18084"
            start_and_check "services/room-service" "RoomServiceApplication" "18081"
            start_and_check "services/booking-service" "BookingServiceApplication" "18083"
          '''
        }
      }
    }

    stage('Archive Artifacts') {
      steps {
        archiveArtifacts artifacts: 'services/**/target/*.jar,ui/dist/**', fingerprint: true, allowEmptyArchive: true
      }
    }
  }

  post {
    always {
      junit testResults: 'services/**/target/surefire-reports/*.xml', allowEmptyResults: true
    }
    success {
      echo 'Pipeline completed successfully.'
    }
    failure {
      echo 'Pipeline failed.'
    }
  }
}
