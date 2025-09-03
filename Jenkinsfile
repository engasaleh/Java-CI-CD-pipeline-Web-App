pipeline {
    agent any

    options {
        // Delete all old builds to save space
        buildDiscarder(logRotator(numToKeepStr: '0'))
        // Optional: Set a maximum runtime for any build
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

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
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
                    echo "Checking UAT app..."
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
                    kubectl get pods -n uat
                    kubectl get svc -n uat
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
                    kubectl set image deployment/web-java-app-deployment web-java-app=${IMAGE_NAME}:${IMAGE_TAG} -n default
                    kubectl rollout status deployment/web-java-app-deployment -n default
                """
            }
        }

        stage('Health Check Production') {
            steps {
                script {
                    echo "Checking Production app..."
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
                    kubectl get pods -n default
                    kubectl get svc -n default
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
                channel: '#devops-alerts',
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' deployed successfully to UAT/Production."
            )
        }
        failure {
            slackSend(
                channel: '#devops-alerts',
                color: 'danger',
                message: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed. Check Jenkins logs."
            )
        }
    }
}

