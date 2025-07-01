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
               git branch: 'docker', url: 'https://github.com/hkhcoder/vprofile-project.git'
            }

	    }


	    stage('Build'){
	        steps{
	           sh 'mvn install -DskipTests'
	        }

	        post {
	           success {
	              echo 'Now Archiving it...'
	              archiveArtifacts artifacts: '**/target/*.war'
	           }
	        }
	    }

	    stage('UNIT TEST') {
            steps{
                sh 'mvn test'
            }
        }

        stage('Checkstyle Analysis') {
            steps{
                sh 'mvn checkstyle:checkstyle'
            }
        }

        stage("Sonar Code Analysis") {
        	environment {
                scannerHome = tool 'SONAR7.1'
            }
            steps {
              withSonarQubeEnv('sonarserver') {
                sh '''${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=vprofile \
                    -Dsonar.projectName=vprofile \
                    -Dsonar.projectVersion=1.0 \
                    -Dsonar.sources=src/ \
                    -Dsonar.java.binaries=target/test-classes/com/visualpathit/account/controllerTest/ \
                    -Dsonar.junit.reportsPath=target/surefire-reports/ \
                    -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                    -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
              }
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true 
                }
            }
        }

        stage("Upload artifacts"){
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: 'nexus.nexus.svc.cluster.local:8081',
                    groupId: 'com.vprofile',
                    version: "${env.BUILD_ID}_${env.BUILD_TIMESTAMP}",
                    repository: 'maven-repo',
                    credentialsId: 'nexus-login',
                    artifacts: [
                        [artifactId: 'vproapp',
                        classifier: '',
                        file: 'target/vprofile-v2.war',
                        type: 'war']
                    ]
                )
            }
        }

        stage('Build app image') {
            steps {
                script {
                    dockerImage = docker.build("$IMAGE_NAME:$BUILD_NUMBER", "./Docker-files/app/multistage")
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