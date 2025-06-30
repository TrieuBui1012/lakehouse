pipeline {
	agent any
	tools {
	    maven "MAVEN3.9"
	    jdk "JDK17"
	}

    environment {
        NEXUS_REPO = 'nexus.nexus.svc.cluster.local:8081/docker-repo'
        IMAGE_NAME = 'vprofile'
    }

	stages {


	    stage('Fetch code') {
            steps {
               git branch: 'main', url: 'https://github.com/TrieuBui1012/lakehouse.git'
            }

	    }

        stage('Build app image') {
            steps {
                script {
                    dockerImage = docker.build("$IMAGE_NAME:$BUILD_NUMBER", "./dockerfiles")
                }
            }
        }

        stage('Upload App Image') {
          steps{
            script {
              docker.withRegistry( NEXUS_REPO, 'nexus-login' ) {
                dockerImage.push("$BUILD_NUMBER")
                dockerImage.push('latest')
              }
            }
          }
        }
	}

    post {
        always {
            emailext body: '''$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS:
            Check console output at $BUILD_URL to view the results.''',
                subject: '$PROJECT_NAME - Build # $BUILD_NUMBER - $BUILD_STATUS!',
                to: 'nhyzzchillax@gmail.com'
        }
    }
}