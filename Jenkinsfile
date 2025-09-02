pipeline {
    agent any

    tools {
        maven 'Maven_3.9.11'   // 👈 Name must match the one you set in "Global Tool Configuration"
    }

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
                // Jenkins will use the Maven tool defined above
                sh 'mvn clean package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t hello-java-app:v1 .'
            }
        }

        stage('Docker Push') {
            steps {
                withDockerRegistry([credentialsId: 'dockerhub-creds', url: '']) {
                    sh 'docker tag hello-java-app:v1 <your-dockerhub-username>/hello-java-app:v2'
                    sh 'docker push <your-dockerhub-username>/hello-java-app:v2'
                }
            }
        }
    }
}
