pipeline {
  agent {
    docker {
      image 'maven:3.9.9-eclipse-temurin-21'
      args '-v $HOME/.m2:/root/.m2'
      reuseNode true
    }
  }

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
          java -version
          mvn -version
        '''
      }
    }

    stage('Build & Test Backend Services') {
      steps {
        sh '''
          set -eux
          for svc in $SERVICE_DIRS; do
            echo "==> Testing $svc"
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
            mvn $MAVEN_ARGS -f "$svc/pom.xml" -DskipTests package
          done
        '''
      }
    }

    stage('Build UI') {
      when {
        expression { return params.BUILD_UI }
      }
      agent {
        docker {
          image 'node:20'
          reuseNode true
        }
      }
      steps {
        dir('ui') {
          sh '''
            set -eux
            node -v
            npm -v
            npm ci
            npm run build
          '''
        }
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
              log_file=".runlogs/${svc_dir##*/}-rds-integration.log"

              echo "==> Starting ${svc_dir} on port ${port} with RDS (${RDS_HOST}:${RDS_PORT})"
              DB_HOST="$RDS_HOST" DB_PORT="$RDS_PORT" DB_USER="$RDS_DB_USER" DB_PASSWORD="$RDS_DB_PASSWORD" \
                mvn $MAVEN_ARGS -f "${svc_dir}/pom.xml" \
                spring-boot:run -Dspring-boot.run.arguments="--server.port=${port}" >"$log_file" 2>&1 &

              pid=$!
              started=0

              for _ in $(seq 1 60); do
                if grep -q "Started ${app_name}" "$log_file"; then
                  started=1
                  break
                fi
                if ! kill -0 "$pid" 2>/dev/null; then
                  echo "${svc_dir} exited before startup. Last logs:"
                  tail -n 120 "$log_file" || true
                  exit 1
                fi
                sleep 2
              done

              if [ "$started" -ne 1 ]; then
                echo "Timed out waiting for ${svc_dir} startup. Last logs:"
                tail -n 120 "$log_file" || true
                kill "$pid" 2>/dev/null || true
                wait "$pid" 2>/dev/null || true
                exit 1
              fi

              echo "${svc_dir} started successfully with RDS."
              kill "$pid" 2>/dev/null || true
              wait "$pid" 2>/dev/null || true
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
