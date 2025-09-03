pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '0')) // لا يحتفظ بأي build قديم
        timeout(time: 1, unit: 'HOURS')
    }

    parameters {
        booleanParam(
            name: 'DEPLOY_TO_PROD',
            defaultValue: false,
            description: 'Check to deploy to Production after UAT'
        )
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
                echo 'Building project and running unit tests...'
                sh 'mvn clean package'
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
            }
        }

        stage('Docker Push') {
            steps {
                echo 'Pushing Docker image to registry...'
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to UAT') {
            steps {
                echo 'Deploying to UAT namespace...'
                sh '''
                    kubectl apply -f deployment/uat/deployment-uat.yaml
                    kubectl apply -f deployment/uat/service-uat.yaml
                '''
            }
        }

      
        stage('Deploy to Production') {
            when {
                expression { params.DEPLOY_TO_PROD }
            }
            steps {
                echo 'Deploying to Production...'
                sh '''
                    kubectl apply -f deployment/production/deployment.yaml
                    kubectl apply -f deployment/production/service.yaml
                '''
            }
        }
    }
}

