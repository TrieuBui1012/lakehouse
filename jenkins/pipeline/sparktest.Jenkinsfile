pipeline {
	agent any
	tools {
	    jdk "JDK11"
	}

	stages {


	    stage('Fetch code') {
            steps {
               git branch: 'main', url: 'https://gitlab.com/trieubui1012-gitops/jenkins-ci.git', credentialsId: 'gitlab-token'
            }

	    }


	    stage('Build'){
	        steps{
                withMaven(mavenSettingsConfig: 'maven-nexus){
                    sh 'mvn install -DskipTests'
                }
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
                withMaven(mavenSettingsConfig: 'maven-nexus){
                    sh 'mvn test'
                }
            }
        }

        stage('Checkstyle Analysis') {
            steps{
                withMaven(mavenSettingsConfig: 'maven-nexus){
                    sh 'mvn checkstyle:checkstyle'
                }
            }
        }

        stage("Sonar Code Analysis") {
            steps {
              withSonarQubeEnv('sonarserver') {
                sh '''sonar-scanner -Dsonar.projectKey=test \
                    -Dsonar.projectName=test \
                    -Dsonar.projectVersion=1.0 \
                    -Dsonar.sources=src/ \
                    -Dsonar.scala.version=2.12 \
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
                    groupId: 'com.sparkapp',
                    version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                    repository: 'maven-releases',
                    credentialsId: 'nexus-login',
                    artifacts: [
                        [artifactId: 'sparkapp',
                        classifier: '',
                        file: 'target/*.jar',
                        type: 'jar']
                    ]
                )
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