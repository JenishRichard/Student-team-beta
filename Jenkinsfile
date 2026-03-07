pipeline {
  agent any

  tools {
    jdk 'JDK17'
    maven 'Maven3'
  }

  options {
    timestamps()
    disableConcurrentBuilds()
  }

  parameters {
    booleanParam(name: 'BUILD_UI', defaultValue: true, description: 'Run UI install/build stages')
    booleanParam(name: 'BUILD_DOCKER', defaultValue: false, description: 'Build Docker images using docker compose')
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
          if [ "${BUILD_UI}" = "true" ]; then
            node -v
            npm -v
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
      steps {
        dir('ui') {
          sh '''
            set -eux
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
      steps {
        sh '''
          set -eux
          docker compose build
        '''
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
