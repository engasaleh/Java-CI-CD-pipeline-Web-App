pipeline {
    agent any

    parameters {
        choice(
            name: 'TARGET_ENV',
            choices: ['UAT', 'Production'],
            description: 'Select the environment to deploy'
        )
    }

    environment {
        KUBECONFIG = '/var/lib/jenkins/.kube/config'
        IMAGE_NAME = 'abdullahsaleh2001/web-java-app'
        IMAGE_TAG = "v1.0.${env.BUILD_NUMBER}"
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

        stage('Approval to Production') {
            when {
                expression { params.TARGET_ENV == 'Production' }
            }
            steps {
                input message: "Approve deployment to Production?"
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def namespace = params.TARGET_ENV.toLowerCase()
                    def deploymentName = namespace == 'uat' ? 'web-java-app-uat' : 'web-java-app-deployment'
                    sh """
                        kubectl set image deployment/${deploymentName} web-java-app=${IMAGE_NAME}:${IMAGE_TAG} -n ${namespace}
                        kubectl rollout status deployment/${deploymentName} -n ${namespace}
                    """
                }
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def port = params.TARGET_ENV == 'UAT' ? 31080 : 30080
                    echo "Checking ${params.TARGET_ENV} app..."
                    def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${port}", returnStdout: true).trim()
                    if (response != '200') {
                        error "${params.TARGET_ENV} health check failed! HTTP status: ${response}"
                    }
                }
            }
        }

        stage('Pod Logs') {
            steps {
                script {
                    def namespace = params.TARGET_ENV.toLowerCase()
                    echo "Fetching ${params.TARGET_ENV} pod logs..."
                    sh """
                        kubectl logs -l app=web-java-app -n ${namespace} --tail=20
                        kubectl get pods -n ${namespace}
                        kubectl get svc -n ${namespace}
                    """
                }
            }
        }

        stage('Rollback if Failed') {
            when {
                expression { currentBuild.result == 'FAILURE' && params.TARGET_ENV == 'Production' }
            }
            steps {
                echo "Rolling back Production to previous stable version..."
                sh "kubectl rollout undo deployment/web-java-app-deployment -n default"
            }
        }
    }

    post {
        success {
            slackSend(
                channel: '#devops-alerts',
                color: 'good',
                message: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' deployed successfully to ${params.TARGET_ENV}."
            )
        }
        failure {
            slackSend(
                channel: '#devops-alerts',
                color: 'danger',
                message: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' failed during deployment to ${params.TARGET_ENV}."
            )
        }
    }
}

