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
        memory: "1Gi"
        cpu: "500m"
      limits:
        memory: "2Gi"
        cpu: "1000m"
  - name: docker
    image: docker:latest
    command:
    - cat
    tty: true
    volumeMounts:
    - name: docker-sock
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-sock
    hostPath:
      path: /var/run/docker.sock
"""
        }
    }

    parameters {
        booleanParam(name: 'FORCE_BUILD_ALL', defaultValue: true, description: 'Force build all services regardless of changes')
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
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Build Gateway Service') {
            when {
                anyOf {
                    changeset "gateway/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Gateway service...'
                dir('gateway') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Build User Service') {
            when {
                anyOf {
                    changeset "user/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building User service...'
                dir('user') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Build Management Service') {
            when {
                anyOf {
                    changeset "management/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Management service...'
                dir('management') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Build Alarm Service') {
            when {
                anyOf {
                    changeset "alarm/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Alarm service...'
                dir('alarm') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Build Sensor Service') {
            when {
                anyOf {
                    changeset "sensor/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Sensor service...'
                dir('sensor') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Build Dashboard Service') {
            when {
                anyOf {
                    changeset "dashboard/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Dashboard service...'
                dir('dashboard') {
                    sh '''
                        chmod +x ../gradlew
                        ../gradlew clean build -x test
                    '''
                }
            }
        }

        stage('Docker Login') {
            steps {
                echo 'Logging into Azure Container Registry with Service Principal...'
                container('docker') {
                    sh '''
                        echo $AZURE_SP_PSW | docker login $ACR_REGISTRY --username $AZURE_SP_USR --password-stdin
                    '''
                }
            }
        }

        stage('Build Gateway Docker Image') {
            when {
                anyOf {
                    changeset "gateway/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Gateway Docker image...'
                dir('gateway') {
                    container('docker') {
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
                anyOf {
                    changeset "user/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building User Docker image...'
                dir('user') {
                    container('docker') {
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
                anyOf {
                    changeset "management/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Management Docker image...'
                dir('management') {
                    container('docker') {
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
                anyOf {
                    changeset "alarm/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Alarm Docker image...'
                dir('alarm') {
                    container('docker') {
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
                anyOf {
                    changeset "sensor/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Sensor Docker image...'
                dir('sensor') {
                    container('docker') {
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
                anyOf {
                    changeset "dashboard/**"
                    changeset "gradlew*"
                    changeset "build.gradle*"
                    changeset "settings.gradle*"
                    expression { return params.FORCE_BUILD_ALL == true }
                }
            }
            steps {
                echo 'Building Dashboard Docker image...'
                dir('dashboard') {
                    container('docker') {
                        sh '''
                            docker build -t ${DASHBOARD_IMAGE} .
                            echo "Built Dashboard image: ${DASHBOARD_IMAGE}"
                        '''
                    }
                }
            }
        }

        stage('Push to ACR') {
            steps {
                echo 'Pushing images to Azure Container Registry...'
                container('docker') {
                    script {
                        if (env.CHANGE_SET?.contains('gateway/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${GATEWAY_IMAGE}'
                        }
                        if (env.CHANGE_SET?.contains('user/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${USER_IMAGE}'
                        }
                        if (env.CHANGE_SET?.contains('management/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${MANAGEMENT_IMAGE}'
                        }
                        if (env.CHANGE_SET?.contains('alarm/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${ALARM_IMAGE}'
                        }
                        if (env.CHANGE_SET?.contains('sensor/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${SENSOR_IMAGE}'
                        }
                        if (env.CHANGE_SET?.contains('dashboard/') || params.FORCE_BUILD_ALL) {
                            sh 'docker push ${DASHBOARD_IMAGE}'
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
                    docker logout ${ACR_REGISTRY}
                    docker image prune -f
                    docker system prune -f --volumes
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
        }

        failure {
            echo 'Build failed!'
        }
    }
}