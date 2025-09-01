pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/<your-username>/hello-java-devops.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t hello-java-app:latest .'
            }
        }

        stage('Docker Push') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh 'docker tag hello-java-app:latest <your-dockerhub-username>/hello-java-app:latest'
                    sh 'docker push <your-dockerhub-username>/hello-java-app:latest'
                }
            }
        }
    }
}

