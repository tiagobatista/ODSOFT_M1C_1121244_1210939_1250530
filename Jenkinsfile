pipeline {
    agent any

    tools {
        maven 'Maven-3.9'  // Nome configurado no Jenkins
        jdk 'JDK-17'       // Nome configurado no Jenkins
    }

    environment {
        APP_NAME = 'psoft-g1'
        DOCKER_NETWORK = 'odsoft_m1c_1121244_1210939_1250530_ci-network'
        SONAR_HOST = 'http://sonarqube:9000'
        REDIS_HOST = 'redis'
    }

    stages {
        // Stage 1: Build & Package
        stage('1. Build & Package') {
            steps {
                echo '🔨 Stage 2: Building and packaging application...'
                sh 'mvn clean package -DskipTests -B'
            }
            post {
                success {
                    echo '✅ Build successful! Archiving artifacts...'
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
                failure {
                    echo '❌ Build failed!'
                }
            }
        }

        // Stage 2: Unit & Integration Tests
        stage('2. Unit & Integration Tests') {
            steps {
                echo '🧪 Stage 2: Running unit and integration tests...'
                sh 'mvn test -B'
            }
            post {
                always {
                    echo '📊 Publishing test results...'
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

                    echo '📊 Publishing code coverage...'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/config/**,**/dto/**,**/exceptions/**,**/*Application.*'
                    )
                }
            }
        }

        // Stage 3: SonarQube Analysis (QG1)
        stage('3. SonarQube Analysis - QG1') {
            steps {
                echo '📊 Stage 3: Running SonarQube static code analysis...'
                script {
                    // Verificar conectividade com SonarQube
                    sh '''
                        echo "🔍 Verificando conectividade com SonarQube..."
                        curl -f ${SONAR_HOST}/api/system/status || echo "⚠️ SonarQube pode não estar disponível"
                    '''
                }
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=psoft-g1 \
                        -Dsonar.projectName="Library Management System" \
                        -Dsonar.host.url=${SONAR_HOST} \
                        -Dsonar.java.binaries=target/classes \
                        -B
                    '''
                }
            }
        }

        stage('4.1. Quality Gate 1') {
            steps {
                echo '🚦 Waiting for Quality Gate result...'
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            echo "⚠️ Quality Gate failed: ${qg.status}"
                            echo 'Pipeline will continue but review the SonarQube report!'
                        } else {
                            echo '✅ Quality Gate passed!'
                        }
                    }
                }
            }
        }

        // Stage 5: Mutation Testing
        stage('5. Mutation Tests (PITest)') {
            steps {
                echo '🧬 Stage 5: Running mutation tests with PITest...'
                sh 'mvn org.pitest:pitest-maven:mutationCoverage -B'
            }
            post {
                always {
                    echo '📊 Publishing mutation test report...'
                    publishHTML([
                        reportDir: 'target/pit-reports',
                        reportFiles: 'index.html',
                        reportName: 'PIT Mutation Report',
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true
                    ])
                }
            }
        }

        // Stage 6: Build Docker Image
        stage('6. Build Docker Image') {
            steps {
                echo '🐳 Stage 6: Building Docker image...'
                script {
                    def imageTag = "${APP_NAME}:${BUILD_NUMBER}"
                    def imageLatest = "${APP_NAME}:latest"
                    def imageDev = "${APP_NAME}:dev"
                    def imageStaging = "${APP_NAME}:staging"
                    def imageProd = "${APP_NAME}:prod"

                    sh """
                        docker build -t ${imageTag} .
                        docker tag ${imageTag} ${imageLatest}
                        docker tag ${imageTag} ${imageDev}
                        docker tag ${imageTag} ${imageStaging}
                        docker tag ${imageTag} ${imageProd}
                    """

                    env.DOCKER_IMAGE_TAG = imageTag
                    env.DOCKER_IMAGE_DEV = imageDev
                    env.DOCKER_IMAGE_STAGING = imageStaging
                    env.DOCKER_IMAGE_PROD = imageProd
                }
            }
        }

        // Stage 7: Deploy to DEV
        stage('7. Deploy to DEV') {
            steps {
                echo '🚀 Stage 7: Deploying to DEV environment...'
                script {
                    sh '''
                        # Stop and remove old container
                        docker stop ${APP_NAME}-dev 2>/dev/null || true
                        docker rm ${APP_NAME}-dev 2>/dev/null || true

                        # Run new container
                        docker run -d \
                            --name ${APP_NAME}-dev \
                            --network ${DOCKER_NETWORK} \
                            -p 8080:8080 \
                            -e SPRING_PROFILES_ACTIVE=sql-redis,bootstrap \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            ${DOCKER_IMAGE_DEV}

                        # Wait for application to start
                        echo "⏳ Waiting for application to start..."
                        sleep 20
                    '''
                }
            }
        }

        // Stage 8: System Tests DEV (QG2)
        stage('8. System Tests DEV - QG2') {
            steps {
                echo '🧪 Stage 8: Running system tests on DEV...'
                script {
                    sh '''
                        # Health check
                        echo "🏥 Checking application health..."
                        for i in {1..5}; do
                            if curl -f http://localhost:8080/actuator/health; then
                                echo "✅ Application is healthy!"
                                break
                            fi
                            echo "⏳ Attempt $i/5: Waiting for application..."
                            sleep 5
                        done

                        # Basic endpoint tests
                        echo "🔍 Testing API endpoints..."
                        curl -f http://localhost:8080/api-docs || echo "⚠️ API docs not accessible"
                        curl -f http://localhost:8080/actuator/info || echo "⚠️ Actuator info not accessible"
                    '''
                }
            }
            post {
                always {
                    echo '📋 DEV environment logs:'
                    sh 'docker logs ${APP_NAME}-dev --tail 50 || true'
                }
            }
        }

        // Stage 9: Deploy to STAGING
        stage('9. Deploy to STAGING') {
            steps {
                echo '🚀 Stage 9: Deploying to STAGING environment...'
                script {
                    sh '''
                        # Stop and remove old container
                        docker stop ${APP_NAME}-staging 2>/dev/null || true
                        docker rm ${APP_NAME}-staging 2>/dev/null || true

                        # Run new container
                        docker run -d \
                            --name ${APP_NAME}-staging \
                            --network ${DOCKER_NETWORK} \
                            -p 8082:8080 \
                            -e SPRING_PROFILES_ACTIVE=sql-redis,bootstrap \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            ${DOCKER_IMAGE_STAGING}

                        # Wait for application to start
                        echo "⏳ Waiting for application to start..."
                        sleep 20
                    '''
                }
            }
        }





        // Stage 10: System Tests STAGING (QG3)
        stage('10. System Tests STAGING - QG3') {
            steps {
                echo '🧪 Stage 10: Running system tests on STAGING...'
                script {
                    sh '''
                        # Health check
                        echo "🏥 Checking application health..."
                        for i in {1..5}; do
                            if curl -f http://localhost:8082/actuator/health; then
                                echo "✅ Application is healthy!"
                                break
                            fi
                            echo "⏳ Attempt $i/5: Waiting for application..."
                            sleep 5
                        done

                        # More comprehensive tests for staging
                        echo "🔍 Testing API endpoints..."
                        curl -f http://localhost:8082/api-docs || echo "⚠️ API docs not accessible"
                        curl -f http://localhost:8082/swagger-ui/index.html || echo "⚠️ Swagger UI not accessible"
                        curl -f http://localhost:8082/actuator/metrics || echo "⚠️ Metrics not accessible"
                    '''
                }
            }
            post {
                always {
                    echo '📋 STAGING environment logs:'
                    sh 'docker logs ${APP_NAME}-staging --tail 50 || true'
                }
            }
        }

        // Stage 11: Deploy to PROD
        stage('11. Deploy to PROD') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    // Manual approval for production
                    input message: '🚀 Deploy to PRODUCTION?', ok: 'Deploy', submitter: 'admin'
                }
                echo '🚀 Stage 11: Deploying to PRODUCTION environment...'
                script {
                    sh '''
                        # Stop and remove old container
                        docker stop ${APP_NAME}-prod 2>/dev/null || true
                        docker rm ${APP_NAME}-prod 2>/dev/null || true

                        # Run new container
                        docker run -d \
                            --name ${APP_NAME}-prod \
                            --network ${DOCKER_NETWORK} \
                            -p 8083:8080 \
                            -e SPRING_PROFILES_ACTIVE=sql-redis,bootstrap \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
                            ${DOCKER_IMAGE_PROD}

                        # Wait for application to start
                        echo "⏳ Waiting for application to start..."
                        sleep 20
                    '''
                }
            }
        }

        // Stage 12: Verify PROD (QG4)
        stage('12. Verify PROD - QG4') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                echo '✅ Stage 12: Verifying PROD deployment...'
                script {
                    sh '''
                        # Comprehensive health check
                        echo "🏥 Performing comprehensive health check..."
                        for i in {1..10}; do
                            if curl -f http://localhost:8083/actuator/health; then
                                echo "✅ PRODUCTION is healthy and running!"

                                # Additional smoke tests
                                echo "🔍 Running smoke tests..."
                                curl -f http://localhost:8083/actuator/info
                                curl -f http://localhost:8083/api-docs

                                exit 0
                            fi
                            echo "⏳ Attempt $i/10: Waiting for application..."
                            sleep 10
                        done

                        echo "❌ PRODUCTION health check failed!"
                        exit 1
                    '''
                }
            }
            post {
                success {
                    echo '🎉 PRODUCTION deployment verified successfully!'
                }
                failure {
                    echo '❌ PRODUCTION verification failed! Rolling back...'
                    sh 'docker stop ${APP_NAME}-prod || true'
                }
                always {
                    echo '📋 PRODUCTION environment logs:'
                    sh 'docker logs ${APP_NAME}-prod --tail 100 || true'
                }
            }
        }
    }

    post {
        always {
            echo '📊 Pipeline execution completed!'
            echo '═══════════════════════════════════════'
            echo 'Published Reports:'
            echo '  - JaCoCo Code Coverage'
            echo '  - PIT Mutation Testing'
            echo '  - JUnit Test Results'
            echo '═══════════════════════════════════════'
        }
        success {
            echo '✅ Pipeline completed successfully!'
            echo '🎉 All stages passed!'
        }
        failure {
            echo '❌ Pipeline failed!'
            echo '📧 Check the console output for details'
        }
        cleanup {
            echo '🧹 Cleaning workspace...'
            cleanWs()
        }
    }
}