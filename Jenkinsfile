pipeline {
    agent any

    environment {
        // Make sure Jenkins can talk to your KIND cluster
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
    }

    stages {
        stage('Cleanup') {
            steps {
                echo 'Cleaning up old workspace, Docker images, and Kubernetes resources...'
                deleteDir() // cleans workspace
                sh '''
                    # Remove old Docker images
                    docker rmi -f web-java-app:v1 || true
                    docker rmi -f abdullahsaleh2001/web-java-app:v2 || true

                    # Remove old Kubernetes deployment and service if exist
                    kubectl delete deployment web-java-app-deployment --ignore-not-found
                    kubectl delete service web-java-app-service --ignore-not-found
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

        stage('Deploy to KIND') {
            steps {
                sh '''
                    kubectl apply -f deployment.yaml
                    kubectl apply -f service.yaml
                '''
            }
        }
    }
}

