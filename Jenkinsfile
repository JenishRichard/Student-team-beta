pipeline {
agent any

```
tools {
    maven 'Maven3'
    jdk 'JDK17'
}

stages {

    stage('Checkout Source Code') {
        steps {
            echo 'Cloning repository from GitHub'
            git branch: 'main', url: 'https://github.com/SanketJr11/Student-team-beta.git'
        }
    }

    stage('Build Project') {
        steps {
            echo 'Compiling project using Maven'
            bat 'mvn clean compile'
        }
    }

    stage('Run Unit Tests') {
        steps {
            echo 'Running unit tests'
            bat 'mvn test'
        }
    }

    stage('Package Application') {
        steps {
            echo 'Packaging application'
            bat 'mvn clean package'
        }
    }

    stage('Archive Artifacts') {
        steps {
            echo 'Saving build artifacts'
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }
    }

}

post {
    success {
        echo 'CI Pipeline completed successfully!'
    }

    failure {
        echo 'Build failed. Please check errors.'
    }

    always {
        junit '**/target/surefire-reports/*.xml'
    }
}
```

}
