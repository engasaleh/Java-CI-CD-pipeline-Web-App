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
        NODE_IP = "127.0.0.1"
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
                junit '**/target/surefire-reports/*.xml'
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
    }

    

