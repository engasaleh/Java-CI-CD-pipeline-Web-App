# Hello DevOps App 🚀

This is a simple Java web application that displays:

    Hello Abdallah, Welcome to you at DevOps Industry.

The goal of this project is to practice **DevOps end-to-end** using:

-   Java + Maven (Application)
-   Docker (Containerization)
-   GitHub (Source Control)
-   Jenkins (CI/CD Automation)
-   DockerHub (Image Registry)
-   KIND Kubernetes Cluster (Deployment)
-   UAT + Production Environments

------------------------------------------------------------------------

## 📂 Project Structure

    hello-devops/
    │── src/                # Java source code
    │── pom.xml             # Maven build file
    │── Dockerfile          # Docker build definition
    │── Jenkinsfile         # Jenkins pipeline definition
    │── deployment/
    │    ├── uat/
    │    │   ├── deployment-uat.yaml
    │    │   └── service-uat.yaml
    │    └── production/
    │        ├── deployment-prod.yaml
    │        └── service-prod.yaml
    │── README.md           # Documentation

------------------------------------------------------------------------

## 📊 CI/CD Pipeline Flow

1.  Developer pushes code to **GitHub**
2.  **Jenkins** is triggered → builds + tests + creates Docker image
3.  Docker image pushed to **DockerHub**
4.  Jenkins deploys to **KIND Cluster**
    -   First to **UAT**
    -   Then to **Production** after approval

------------------------------------------------------------------------

## 🚀 How to Run Locally

### 1. Build with Maven

``` bash
mvn clean package
```

### 2. Build Docker Image

``` bash
docker build -t hello-java-app:latest .
```

### 3. Run Docker Locally

``` bash
docker run -p 8080:8080 hello-java-app:latest
```

Then open: <http://localhost:8080>

------------------------------------------------------------------------

## 👨‍💻 Author

-   **Abdallah Saleh**
-   Practicing DevOps CI/CD with Jenkins + Kubernetes
