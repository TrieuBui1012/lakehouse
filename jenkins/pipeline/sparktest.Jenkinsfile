pipeline {
	agent any
	tools {
	    jdk "JDK11"
	}

	stages {


	    stage('Fetch code') {
            steps {
               git branch: 'main', url: 'https://gitlab.com/trieubui1012-gitops/jenkins-ci.git', credentialsId: 'gitlab-login'
            }

	    }


	    stage('Build'){
	        steps{
                updateGitlabCommitStatus name: 'build', state: 'pending'
                withMaven(mavenSettingsConfig: 'maven-nexus'){
                    sh 'mvn install -DskipTests'
                }
	        }

	        post {
	           success {
	              echo 'Now Archiving it...'
	              archiveArtifacts artifacts: '**/target/*.jar'
	           }
	        }
	    }

	    // stage('UNIT TEST') {
        //     steps{
        //         withMaven(mavenSettingsConfig: 'maven-nexus'){
        //             sh 'mvn test'
        //         }
        //     }
        // }

        stage('Checkstyle Analysis') {
            steps{
                withMaven(mavenSettingsConfig: 'maven-nexus'){
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
                        [artifactId: 'sparkappwithdeps',
                        classifier: '',
                        file: 'target/spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar',
                        type: 'jar'],
                        [artifactId: 'sparkapp',
                        classifier: '',
                        file: 'target/spark8s-1.0-SNAPSHOT.jar',
                        type: 'jar']
                    ]
                )
                minio bucket: 'cicd', credentialsId: 'minio', excludes: '', host: 'https://s3.cloudfly.vn', includes: '**/target/*.jar', targetFolder: 'spark'
            }
        }

        stage('Checkout ArgoCD repo') {
            steps {
                deleteDir()
                checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitlab-login', url: 'https://gitlab.com/trieubui1012-gitops/spark-yaml.git']])
            }
        }

        stage('Update ArgoCD YAML') {
            steps {
                script {
                    sh '''
                    yq -i '.spec.mainApplicationFile = "s3://cicd/spark/spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar"' spark-test.yaml
                    '''
                }
                sh 'git add .'
                sh 'git commit -m "Update spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar artifact version to ${env.BUILD_ID}-${env.BUILD_TIMESTAMP}"'
                withCredentials([gitUsernamePassword(credentialsId: 'gitlab-login',
                    gitToolName: 'git-tool')]) {
                    sh 'git push'
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
        success {
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        failure {
            updateGitlabCommitStatus name: 'build', state: 'failed'
        }
        aborted {
            updateGitlabCommitStatus name: 'build', state: 'canceled'
        }
    }
}