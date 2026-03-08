pipeline {
    agent any

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
                    sh 'mvn clean test package'
                }
            }
        }

        stage('Build booking-service') {
            steps {
                dir('services/booking-service') {
                    sh 'mvn clean test package'
                }
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'services/**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'services/**/target/*.jar', fingerprint: true
        }

        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}