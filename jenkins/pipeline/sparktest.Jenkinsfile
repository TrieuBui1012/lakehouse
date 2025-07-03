pipeline {
	agent any
	tools {
	    jdk "JDK11"
	}

	stages {


	    // stage('Fetch code') {
        //     steps {
        //        git branch: 'main', url: 'https://gitlab.com/trieubui1012-gitops/jenkins-ci.git', credentialsId: 'gitlab-login'
        //     }

	    // }


	    // stage('Build'){
	    //     steps{
        //         updateGitlabCommitStatus name: 'build', state: 'pending'
        //         withMaven(mavenSettingsConfig: 'maven-nexus'){
        //             sh 'mvn -v'
        //             sh 'mvn install -DskipTests'
        //         }
	    //     }

	    //     post {
	    //        success {
	    //           echo 'Now Archiving it...'
	    //           archiveArtifacts artifacts: '**/target/*.jar'
        //           updateGitlabCommitStatus name: 'build', state: 'success'
	    //        }
	    //     }
	    // }

	    // stage('UNIT TEST') {
        //     steps{
        //         echo 'Running unit tests...'
        //         updateGitlabCommitStatus name: 'test', state: 'pending'
        //         withMaven(mavenSettingsConfig: 'maven-nexus'){
        //             sh 'mvn test'
        //         }
        //         updateGitlabCommitStatus name: 'test', state: 'success'
        //     }
        // }

        // stage('Checkstyle Analysis') {
        //     steps{
        //         echo 'Running Checkstyle analysis...'
        //         updateGitlabCommitStatus name: 'checkstyle', state: 'pending'
        //         withMaven(mavenSettingsConfig: 'maven-nexus'){
        //             sh 'mvn checkstyle:checkstyle'
        //         }
        //         updateGitlabCommitStatus name: 'checkstyle', state: 'success'
        //     }
        // }

        // stage("Sonar Code Analysis") {
        //     steps {
        //         echo 'Running SonarQube analysis...'
        //         updateGitlabCommitStatus name: 'sonarqube', state: 'pending
        //       withSonarQubeEnv('sonarserver') {
        //         sh '''sonar-scanner -Dsonar.projectKey=test \
        //             -Dsonar.projectName=test \
        //             -Dsonar.projectVersion=1.0 \
        //             -Dsonar.sources=src/ \
        //             -Dsonar.scala.version=2.12 \
        //             -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml'''
        //         }
        //         updateGitlabCommitStatus name: 'sonarqube', state: 'success'
        //     }
        // }

        // stage("Quality Gate") {
        //     steps {
        //         echo 'Waiting for SonarQube Quality Gate...'
        //         updateGitlabCommitStatus name: 'qualitygate', state: 'pending'
        //         timeout(time: 30, unit: 'MINUTES') {
        //             waitForQualityGate abortPipeline: true 
        //         }
        //        updateGitlabCommitStatus name: 'qualitygate', state: 'success'
        //     }
        // }

        stage("Trivy Scan") {
            steps {
                echo 'Running Trivy scan...'
                updateGitlabCommitStatus name: 'trivy-scan', state: 'pending'
                sh 'trivy fs --offline-scan --no-progress --exit-code 1 --severity HIGH,CRITICAL --format table . | tee trivy-scan-report.txt'
            }
        }

        // stage("Upload artifacts"){
        //     steps {
        //         updateGitlabCommitStatus name: 'upload-artifacts', state: 'pending'
        //         echo 'Uploading artifacts...'
        //         // echo 'Uploading artifacts to Nexus...'
        //         // nexusArtifactUploader(
        //         //     nexusVersion: 'nexus3',
        //         //     protocol: 'http',
        //         //     nexusUrl: 'nexus.nexus.svc.cluster.local:8081',
        //         //     groupId: 'com.sparkapp',
        //         //     version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
        //         //     repository: 'maven-releases',
        //         //     credentialsId: 'nexus-login',
        //         //     artifacts: [
        //         //         [artifactId: 'sparkappwithdeps',
        //         //         classifier: '',
        //         //         file: 'target/spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar',
        //         //         type: 'jar'],
        //         //         [artifactId: 'sparkapp',
        //         //         classifier: '',
        //         //         file: 'target/spark8s-1.0-SNAPSHOT.jar',
        //         //         type: 'jar']
        //         //     ]
        //         // )
        //         echo 'Uploading artifacts to Minio...'
        //         minio bucket: 'cicd', credentialsId: 'minio', excludes: '', host: 'http://minio.minio-dev.svc.cluster.local:9000', includes: '**/target/*.jar,trivy-scan-report.txt', targetFolder: 'spark'
        //         updateGitlabCommitStatus name: 'upload-artifacts', state: 'success'
        //     }
        // }

        // stage('Checkout ArgoCD repo') {
        //     steps {
        //         updateGitlabCommitStatus name: 'checkout-argocd', state: 'pending'
        //         deleteDir()
        //         checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[credentialsId: 'gitlab-login', url: 'https://gitlab.com/trieubui1012-gitops/spark-yaml.git']])
        //         updateGitlabCommitStatus name: 'checkout-argocd', state: 'success'
        //     }
        // }

        // stage('Update ArgoCD YAML') {
        //     steps {
        //         updateGitlabCommitStatus name: 'update-yaml', state: 'pending'
        //         sh 'git checkout main'
        //         sh '''
        //         git config --local user.email "trieubqt1012@gmail.com"
        //         git config --local user.name "TrieuBui1012"
        //         '''
        //         sh '''
        //             yq -i '.spec.mainApplicationFile = "s3://cicd/spark/spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar"' spark-test.yaml
        //             '''
        //         sh 'git add .'
        //         sh """
        //         git commit -m "Update spark8s-1.0-SNAPSHOT-jar-with-dependencies.jar artifact version to ${env.BUILD_ID}-${env.BUILD_TIMESTAMP}"
        //         """
        //         withCredentials([gitUsernamePassword(credentialsId: 'gitlab-login',
        //             gitToolName: 'git-tool')]) {
        //             sh 'git push origin main'
        //         }
        //         updateGitlabCommitStatus name: 'update-yaml', state: 'success'
        //     }  
        // }
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