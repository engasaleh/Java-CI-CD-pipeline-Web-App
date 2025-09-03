pipeline {
    agent any

    environment {
        // Kubernetes config for Jenkins to talk to KIND or other clusters
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
        IMAGE_NAME = 'abdullahsaleh2001/web-java-app'
        IMAGE_TAG = "v1.0.${env.BUILD_NUMBER}"
        UAT_NODEPORT = "31080"
        PROD_NODEPORT = "30080"
        NODE_IP = "127.0.0.1" // change if Jenkins is not on the same host as KIND
        SLACK_CHANNEL = '#devops-alerts'
    }

    options {
        // Keep last 10 builds and timeout after 1 hour
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 1, unit: 'HOURS')
    }

    stages {

        stage('Cleanup') {
            steps {
                echo 'Cleaning up old workspace and Docker images...'
                deleteDir()
                sh """
                    docker rmi -f ${IMAGE_NAME}:${IMAGE_TAG} || true
                """
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/engasaleh/hello-devops.git'
            }
        }

        stage('Build & Unit Tests') {
            steps {
                echo 'Building project and running unit tests...'
                sh 'mvn clean package'
                sh 'mvn test'
            }
        }

        stage('Static Code Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                // Assuming SonarQube is configured in Jenkins
                withSonarQubeEnv('SonarQube') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=web-java-app -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Security Scan') {
            steps {
                echo 'Scanning Docker image for vulnerabilities...'
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
                    kubectl apply -f k8s/uat/namespace.yaml || true
                    kubectl apply -f k8s/uat/deployment.yaml
                    kubectl apply -f k8s/uat/service.yaml
                    kubectl rollout status deployment/web-java-app-uat -n uat
                """
            }
        }

        stage('Health Check UAT') {
            steps {
                script {
                    echo "Checking UAT app..."
                    sleep 5
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://${NODE_IP}:${UAT_NODEPORT}",
                        returnStdout: true
                    ).trim()
                    if (response != '200') {
                        error "UAT health check failed! HTTP status: ${response}"
                    }
                }
            }
        }

        stage('UAT Pod Logs') {
            steps {
                sh """
                    echo "Fetching UAT pod logs..."
                    kubectl logs -l app=web-java-app -n uat --tail=20
                """
            }
        }

        stage('Approval to Production') {
            steps {
                input message: "Approve deployment to Production?"
            }
        }

        stage('Deploy to Production') {
            steps {
                echo 'Deploying to Production...'
                sh """
                    kubectl apply -f k8s/production/deployment.yaml
                    kubectl apply -f k8s/production/service.yaml
                    kubectl rollout status deployment/web-java-app-deployment -n default
                """
            }
        }

        stage('Health Check Production') {
            steps {
                script {
                    echo "Checking Production app..."
                    sleep 5
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://${NODE_IP}:${PROD_NODEPORT}",
                        returnStdout: true
                    ).trim()
                    if (response != '200') {
                        error "Production health check failed! HTTP status: ${response}"
                    }
                }
            }
        }

        stage('Production Pod Logs') {
            steps {
                sh """
                    echo "Fetching Production pod logs..."
                    kubectl logs -l app=web-java-app -n default --tail=20
                """
            }
        }

        stage('Rollback if Failed') {
            when {
                expression { currentBuild.result == 'FAILURE' }
            }
            steps {
                echo "Rolling back to previous stable version..."
                sh "kubectl rollout undo deployment/web-java-app-deployment -n default"
            }
        }
    }

    post {
        success {
            slackSend(
                channel: "${SLACK_CHANNEL}",
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' deployed successfully to UAT/Production."
            )
        }
        failure {
            slackSend(
                channel: "${SLACK_CHANNEL}",
                color: 'danger',
                message: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed. Check Jenkins logs."
            )
        }
    }
}

