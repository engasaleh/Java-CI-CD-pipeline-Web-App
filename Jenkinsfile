pipeline {
    agent any

    stages {
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
                // Build Docker image locally as v1
                sh 'docker build -t web-java-app:v1 .'
            }
        }

        stage('Docker Push') {
            steps {
                // Push Docker image to Docker Hub as v2
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh 'docker tag web-java-app:v1 abdullahsaleh2001/web-java-app:v2'
                    sh 'docker push abdullahsaleh2001/web-java-app:v2'
                }
            }
        }
    }
}
