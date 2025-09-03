pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '0'))
        timeout(time: 1, unit: 'HOURS')
    }

    environment {
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
        IMAGE_NAME = 'abdullahsaleh2001/web-java-app'
        IMAGE_TAG = "v1.0.${env.BUILD_NUMBER}"
        UAT_NODEPORT = "31080"
        PROD_NODEPORT = "30080"
        NODE_IP = "127.0.0.1" // change if Jenkins is not on the same host as KIND
    }

    stages {

        stage('Cleanup') {
            steps {
                echo 'Cleaning workspace and old Docker images...'
                deleteDir()
                sh "docker rmi -f ${IMAGE_NAME}:${IMAGE_TAG} || true"
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/engasaleh/hello-devops.git'
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn clean package'
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml' // Collect test reports
            }
        }

        stage('Code Quality Scan') {
            steps {
                // Run SonarQube scan (requires SonarQube Jenkins plugin)
                withSonarQubeEnv('sonarqube') {
                    sh "mvn sonar:sonar"
                }
            }
        }

        stage('Docker Build & Scan') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                // Scan Docker image for vulnerabilities using Trivy
                sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Docker Push') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to UAT') {
            steps {
                echo 'Deploying to UAT namespace...'
                sh """
                    kubectl set image deployment/web-java-app-uat web-java-app=${IMAGE_NAME}:${IMAGE_TAG} -n uat
                    kubectl rollout status deployment/web-java-app-uat -n uat
                """
            }
        }

        stage('Health Check UAT') {
            steps {
                script {
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:${UAT_NODEPORT}",
                        returnStdout: true
                    ).trim()
                    if (response != '200') {
                        error "UAT health check failed! HTTP status: ${response}"
                    }
                }
            }
        }

        stage('Approval to Production') {
            steps {
                input message: "Approve deployment to Production?"
            }
        }

        stage('Deploy to Production') {
            steps {
                sh """
                    kubectl set image deployment/web-java-app-deployment web-java-app=${IMAGE_NAME}:${IMAGE_TAG} -n default
                    kubectl rollout status deployment/web-java-app-deployment -n default
                """
            }
        }

        stage('Health Check Production') {
            steps {
                script {
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:${PROD_NODEPORT}",
                        returnStdout: true
                    ).trim()
                    if (response != '200') {
                        error "Production health check failed! HTTP status: ${response}"
                    }
                }
            }
        }

        stage('Fetch Production Logs') {
            steps {
                sh """
                    kubectl logs -l app=web-java-app -n default --tail=50
                    kubectl get pods -n default
                """
            }
        }

        stage('Rollback if Failed') {
            when {
                expression { currentBuild.result == 'FAILURE' }
            }
            steps {
                sh "kubectl rollout undo deployment/web-java-app-deployment -n default"
            }
        }
    }

    post {
        success {
            slackSend(
                channel: '#devops-alerts',
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' deployed successfully."
            )
        }
        failure {
            slackSend(
                channel: '#devops-alerts',
                color: 'danger',
                message: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed! Check Jenkins logs."
            )
        }
    }
}

