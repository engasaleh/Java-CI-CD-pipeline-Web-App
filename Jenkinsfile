pipeline {
    agent any

    environment {
        // Make sure Jenkins can talk to your KIND cluster
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
    }

    stages {

        stage('Cleanup') {
            steps {
                echo 'Cleaning up old workspace and Docker images...'
                deleteDir()
                sh '''
                    docker rmi -f web-java-app:v1 || true
                    docker rmi -f abdullahsaleh2001/web-java-app:v2 || true
                '''
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
                sh 'docker build -t web-java-app:v1 .'
            }
        }

        stage('Docker Push') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh '''
                        docker tag web-java-app:v1 abdullahsaleh2001/web-java-app:v2
                        docker push abdullahsaleh2001/web-java-app:v2
                    '''
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

        stage('Approval to Production') {
            steps {
                input message: "Approve deployment to Production?"
            }
        }

        stage('Deploy to Production') {
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

