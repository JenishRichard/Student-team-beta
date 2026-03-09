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

        stage('Check test reports') {
            steps {
                sh '''
                    echo "Checking surefire reports..."
                    find services -type d -name "surefire-reports" || true
                    find services -type f -path "*/target/surefire-reports/*.xml" || true
                '''
            }
        }
    }

    post {
        always {
        junit allowEmptyResults: true, testResults: '''
            services/room-service/target/surefire-reports/*.xml,
            services/booking-service/target/surefire-reports/*.xml
        '''
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