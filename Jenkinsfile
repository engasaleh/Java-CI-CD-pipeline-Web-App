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
                sh 'sudo docker build -t hello-java-app:v1 .'
            }
        }

        stage('Docker Push') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh 'sudo docker tag hello-java-app:v1 <your-dockerhub-username>/hello-java-app:v2'
                    sh 'sudo docker push <your-dockerhub-username>/hello-java-app:v2'
                }
            }
        }
    }
}
