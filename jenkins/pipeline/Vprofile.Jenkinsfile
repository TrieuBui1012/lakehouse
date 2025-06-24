pipeline {
    agent any
    tools {
        maven "MAVEN3.9"
        jdk "JDK17"
    }
    stages {
        stage('Fetch code'){
            steps {
                git branch: 'atom', url: 'https://github.com/hkhcoder/vprofile-project.git'
            }
        }
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Build'){
            steps {
                sh 'mvn install -DskipTests'
            }
            post {
                success {
                    echo "Archive artifact"
                    archiveArtifacts artifacts: 'target/*.war'
                }
            }
        }
    }
}