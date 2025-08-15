pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:3327.v868139a_d00e0-2
    resources:
      requests:
        memory: "512Mi"
        cpu: "200m"
      limits:
        memory: "1Gi"
        cpu: "500m"
  - name: docker
    image: docker:dind
    securityContext:
      privileged: true
      runAsUser: 0
    env:
    - name: DOCKER_TLS_CERTDIR
      value: ""
    - name: DOCKER_HOST
      value: "tcp://localhost:2375"
    ports:
    - containerPort: 2375
    resources:
      requests:
        memory: "512Mi"
        cpu: "200m"
      limits:
        memory: "1Gi"
        cpu: "500m"
  - name: docker-client
    image: docker:latest
    command:
    - cat
    tty: true
    env:
    - name: DOCKER_HOST
      value: "tcp://localhost:2375"
    securityContext:
      runAsUser: 0
  - name: kubectl
    image: alpine/k8s:1.28.2
    command:
    - sleep
    - infinity
"""
        }
    }

    parameters {
        booleanParam(name: 'FORCE_BUILD_ALL', defaultValue: false, description: 'Force build all services regardless of changes')
        booleanParam(name: 'DEPLOY_GATEWAY', defaultValue: true, description: 'Deploy Gateway service')
        booleanParam(name: 'DEPLOY_USER', defaultValue: true, description: 'Deploy User service')
        booleanParam(name: 'DEPLOY_MANAGEMENT', defaultValue: true, description: 'Deploy Management service')
        booleanParam(name: 'DEPLOY_ALARM', defaultValue: true, description: 'Deploy Alarm service')
        booleanParam(name: 'DEPLOY_SENSOR', defaultValue: true, description: 'Deploy Sensor service')
        booleanParam(name: 'DEPLOY_DASHBOARD', defaultValue: true, description: 'Deploy Dashboard service')
        booleanParam(name: 'DEPLOY_FRONTEND', defaultValue: true, description: 'Deploy Frontend service')
    }

    environment {
        // Azure Container Registry 정보
        ACR_REGISTRY = 'iroomregistry.azurecr.io'

        // Azure Service Principal 자격 증명
        AZURE_SP = credentials('azure-credentials') // Jenkins에 등록된 Azure Service Principal

        // 빌드 버전 (timestamp 기반)
        BUILD_VERSION = "${new Date().format('yyyyMMddHHmm')}"

        // Docker 이미지 태그
        GATEWAY_IMAGE = "${ACR_REGISTRY}/gateway:${BUILD_VERSION}"
        USER_IMAGE = "${ACR_REGISTRY}/user:${BUILD_VERSION}"
        MANAGEMENT_IMAGE = "${ACR_REGISTRY}/management:${BUILD_VERSION}"
        ALARM_IMAGE = "${ACR_REGISTRY}/alarm:${BUILD_VERSION}"
        SENSOR_IMAGE = "${ACR_REGISTRY}/sensor:${BUILD_VERSION}"
        DASHBOARD_IMAGE = "${ACR_REGISTRY}/dashboard:${BUILD_VERSION}"
        FRONTEND_IMAGE = "${ACR_REGISTRY}/frontend:${BUILD_VERSION}"
        FRONTEND_WORKER_IMAGE = "${ACR_REGISTRY}/frontend-worker:${BUILD_VERSION}"

        // Build flags
        GATEWAY_BUILT = "false"
        USER_BUILT = "false"
        MANAGEMENT_BUILT = "false"
        ALARM_BUILT = "false"
        SENSOR_BUILT = "false"
        DASHBOARD_BUILT = "false"
        FRONTEND_BUILT = "false"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                script {
                    env.GIT_DIFF = sh(script: 'git diff --name-only HEAD~1 HEAD || echo ""', returnStdout: true).trim()
                    echo "Git changes detected: ${env.GIT_DIFF}"
                }
            }
        }

        stage('Build Gateway Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_GATEWAY == true }
                    anyOf {
                        changeset "gateway/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.GATEWAY_BUILT = "true"
                    echo 'Building Gateway service...'
                    dir('gateway') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }

        stage('Build User Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_USER == true }
                    anyOf {
                        changeset "user/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.USER_BUILT = "true"
                    echo 'Building User service...'
                    dir('user') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }

        stage('Build Management Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_MANAGEMENT == true }
                    anyOf {
                        changeset "management/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.MANAGEMENT_BUILT = "true"
                    echo 'Building Management service...'
                    dir('management') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }

        stage('Build Alarm Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_ALARM == true }
                    anyOf {
                        changeset "alarm/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.ALARM_BUILT = "true"
                    echo 'Building Alarm service...'
                    dir('alarm') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }

        stage('Build Sensor Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_SENSOR == true }
                    anyOf {
                        changeset "sensor/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.SENSOR_BUILT = "true"
                    echo 'Building Sensor service...'
                    dir('sensor') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }

        stage('Build Dashboard Service') {
            when {
                allOf {
                    expression { return params.DEPLOY_DASHBOARD == true }
                    anyOf {
                        changeset "dashboard/**"
                        changeset "gradlew*"
                        changeset "build.gradle*"
                        changeset "settings.gradle*"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.DASHBOARD_BUILT = "true"
                    echo 'Building Dashboard service...'
                    dir('dashboard') {
                        sh '''
                            chmod +x ../gradlew
                            ../gradlew clean build -x test
                        '''
                    }
                }
            }
        }


        stage('Docker Login') {
            steps {
                echo 'Logging into Azure Container Registry with Service Principal...'
                container('docker-client') {
                    sh '''
                        echo $AZURE_SP_PSW | docker login $ACR_REGISTRY --username $AZURE_SP_USR --password-stdin
                    '''
                }
            }
        }

        stage('Build Gateway Docker Image') {
            when {
                expression { return env.GATEWAY_BUILT == "true" }
            }
            steps {
                echo 'Building Gateway Docker image...'
                dir('gateway') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${GATEWAY_IMAGE} .
                            echo "Built Gateway image: ${GATEWAY_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build User Docker Image') {
            when {
                expression { return env.USER_BUILT == "true" }
            }
            steps {
                echo 'Building User Docker image...'
                dir('user') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${USER_IMAGE} .
                            echo "Built User image: ${USER_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build Management Docker Image') {
            when {
                expression { return env.MANAGEMENT_BUILT == "true" }
            }
            steps {
                echo 'Building Management Docker image...'
                dir('management') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${MANAGEMENT_IMAGE} .
                            echo "Built Management image: ${MANAGEMENT_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build Alarm Docker Image') {
            when {
                expression { return env.ALARM_BUILT == "true" }
            }
            steps {
                echo 'Building Alarm Docker image...'
                dir('alarm') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${ALARM_IMAGE} .
                            echo "Built Alarm image: ${ALARM_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build Sensor Docker Image') {
            when {
                expression { return env.SENSOR_BUILT == "true" }
            }
            steps {
                echo 'Building Sensor Docker image...'
                dir('sensor') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${SENSOR_IMAGE} .
                            echo "Built Sensor image: ${SENSOR_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build Dashboard Docker Image') {
            when {
                expression { return env.DASHBOARD_BUILT == "true" }
            }
            steps {
                echo 'Building Dashboard Docker image...'
                dir('dashboard') {
                    container('docker-client') {
                        sh '''
                            docker build -t ${DASHBOARD_IMAGE} .
                            echo "Built Dashboard image: ${DASHBOARD_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Build Frontend Admin Docker Image') {
            when {
                allOf {
                    expression { return params.DEPLOY_FRONTEND == true }
                    anyOf {
                        changeset "frontend/**"
                        expression { return params.FORCE_BUILD_ALL == true }
                    }
                }
            }
            steps {
                script {
                    env.FRONTEND_BUILT = "true"
                    echo 'Building Frontend Admin Docker image...'
                    dir('frontend') {
                        container('docker-client') {
                            sh '''
                                docker build -t ${FRONTEND_IMAGE} .
                                echo "Built Frontend Admin image: ${FRONTEND_IMAGE}"
                            '''
                        }
                    }
                }
            }
        }

        stage('Build Frontend Worker Docker Image') {
            when {
                expression { return env.FRONTEND_BUILT == "true" }
            }
            steps {
                echo 'Building Frontend Worker Docker image...'
                dir('frontend') {
                    container('docker-client') {
                        sh '''
                            docker build -f Dockerfile.worker -t ${FRONTEND_WORKER_IMAGE} .
                            echo "Built Frontend Worker image: ${FRONTEND_WORKER_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Push to ACR') {
            steps {
                echo 'Pushing images to Azure Container Registry...'
                container('docker-client') {
                    script {
                        if (params.DEPLOY_GATEWAY == true && env.GATEWAY_BUILT == "true") {
                            sh "docker push ${GATEWAY_IMAGE}"
                        }
                        if (params.DEPLOY_USER == true && env.USER_BUILT == "true") {
                            sh "docker push ${USER_IMAGE}"
                        }
                        if (params.DEPLOY_MANAGEMENT == true && env.MANAGEMENT_BUILT == "true") {
                            sh "docker push ${MANAGEMENT_IMAGE}"
                        }
                        if (params.DEPLOY_ALARM == true && env.ALARM_BUILT == "true") {
                            sh "docker push ${ALARM_IMAGE}"
                        }
                        if (params.DEPLOY_SENSOR == true && env.SENSOR_BUILT == "true") {
                            sh "docker push ${SENSOR_IMAGE}"
                        }
                        if (params.DEPLOY_DASHBOARD == true && env.DASHBOARD_BUILT == "true") {
                            sh "docker push ${DASHBOARD_IMAGE}"
                        }
                        if (params.DEPLOY_FRONTEND == true && env.FRONTEND_BUILT == "true") {
                            sh "docker push ${FRONTEND_IMAGE}"
                            sh "docker push ${FRONTEND_WORKER_IMAGE}"
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                echo 'Deploying to Kubernetes cluster...'
                container('kubectl') {
                    withKubeConfig([credentialsId: 'kubeconfig']) {
                        script {
                            // Gateway 배포
                            if (params.DEPLOY_GATEWAY == true && env.GATEWAY_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/gateway-deployment gateway=${GATEWAY_IMAGE} --namespace=default
                                    kubectl rollout status deployment/gateway-deployment --namespace=default --timeout=300s
                                """
                            }

                            // User 서비스 배포
                            if (params.DEPLOY_USER == true && env.USER_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/user-deployment user=${USER_IMAGE} --namespace=default
                                    kubectl rollout status deployment/user-deployment --namespace=default --timeout=300s
                                """
                            }

                            // Management 서비스 배포
                            if (params.DEPLOY_MANAGEMENT == true && env.MANAGEMENT_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/management-deployment management=${MANAGEMENT_IMAGE} --namespace=default
                                    kubectl rollout status deployment/management-deployment --namespace=default --timeout=300s
                                """
                            }

                            // Alarm 서비스 배포
                            if (params.DEPLOY_ALARM == true && env.ALARM_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/alarm-deployment alarm=${ALARM_IMAGE} --namespace=default
                                    kubectl rollout status deployment/alarm-deployment --namespace=default --timeout=300s
                                """
                            }

                            // Sensor 서비스 배포
                            if (params.DEPLOY_SENSOR == true && env.SENSOR_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/sensor-deployment sensor=${SENSOR_IMAGE} --namespace=default
                                    kubectl rollout status deployment/sensor-deployment --namespace=default --timeout=300s
                                """
                            }

                            // Dashboard 서비스 배포
                            if (params.DEPLOY_DASHBOARD == true && env.DASHBOARD_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/dashboard-deployment dashboard=${DASHBOARD_IMAGE} --namespace=default
                                    kubectl rollout status deployment/dashboard-deployment --namespace=default --timeout=300s
                                """
                            }

                            // Frontend 서비스 배포
                            if (params.DEPLOY_FRONTEND == true && env.FRONTEND_BUILT == "true") {
                                sh """
                                    kubectl set image deployment/frontend-deployment frontend=${FRONTEND_IMAGE} --namespace=default
                                    kubectl rollout status deployment/frontend-deployment --namespace=default --timeout=300s
                                    kubectl set image deployment/frontend-worker-deployment frontend-worker=${FRONTEND_WORKER_IMAGE} --namespace=default
                                    kubectl rollout status deployment/frontend-worker-deployment --namespace=default --timeout=300s
                                """
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline completed.'
            // Docker logout and cleanup
            container('docker') {
                sh '''
                    docker logout ${ACR_REGISTRY} || true
                    docker image prune -f || true
                    docker system prune -f --volumes || true
                '''
            }
        }

        success {
            echo 'Build successful!'
            echo "Built images:"
            echo "Gateway: ${GATEWAY_IMAGE}"
            echo "User: ${USER_IMAGE}"
            echo "Management: ${MANAGEMENT_IMAGE}"
            echo "Alarm: ${ALARM_IMAGE}"
            echo "Sensor: ${SENSOR_IMAGE}"
            echo "Dashboard: ${DASHBOARD_IMAGE}"
            echo "Frontend: ${FRONTEND_IMAGE}"
            echo "Frontend Worker: ${FRONTEND_WORKER_IMAGE}"
        }

        failure {
            echo 'Build failed!'
        }
    }
}
