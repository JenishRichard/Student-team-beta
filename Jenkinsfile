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

    post {
        always {
            junit testResults: 'services/**/target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'services/**/target/*.jar, services/**/target/surefire-reports/*.xml, services/**/target/site/jacoco/**, fingerprint: true
        }

        success {
            echo 'Pipeline completed successfully.'
        }

        failure {
            echo 'Pipeline failed.'
        }
    }
}