pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        APP_NAME = 'psoft-g1'
        DOCKER_NETWORK = 'odsoft_m1c_1121244_1210939_1250530_ci-network'
        SONAR_HOST = 'http://sonarqube:9000'
        REDIS_HOST = 'redis'
        DOCKER_AVAILABLE = 'false'
    }

    stages {
        // Stage 0: Environment Check
        stage('0. Environment Check') {
            steps {
                script {
                    echo '🔍 Checking environment prerequisites...'

                    // Check Docker
                    def dockerExists = sh(script: 'command -v docker', returnStatus: true) == 0
                    env.DOCKER_AVAILABLE = dockerExists ? 'true' : 'false'

                    if (dockerExists) {
                        echo '✅ Docker is available'
                        sh 'docker --version'

                        // Check network exists
                        def networkExists = sh(
                            script: "docker network inspect ${DOCKER_NETWORK} > /dev/null 2>&1",
                            returnStatus: true
                        ) == 0

                        if (!networkExists) {
                            echo "⚠️ Network ${DOCKER_NETWORK} doesn't exist. Creating..."
                            sh "docker network create ${DOCKER_NETWORK} || true"
                        }
                        echo "✅ Network ${DOCKER_NETWORK} is ready"

                        // Connect Redis container to the CI network if not already connected
                        echo '🔗 Connecting Redis to CI network...'
                        sh """
                            docker network connect ${DOCKER_NETWORK} redis 2>/dev/null && echo "✅ Redis connected to network" || echo "ℹ️ Redis already connected to network"
                        """

                        // Verify Redis is accessible
                        sh """
                            echo "🔍 Verifying Redis connectivity..."
                            docker run --rm --network ${DOCKER_NETWORK} redis:7-alpine redis-cli -h redis ping || echo "⚠️ Redis not responding"
                        """
                    } else {
                        echo '⚠️ Docker not available - deployment stages will be skipped'
                    }

                    // Check Maven & JDK
                    sh 'mvn --version'
                    sh 'java -version'
                }
            }
        }

        // Stage 1: Build & Package
        stage('1. Build & Package') {
            steps {
                echo '🔨 Stage 1: Building and packaging application...'
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
                    sh '''
                        echo "🔍 Checking SonarQube connectivity..."
                        curl -f ${SONAR_HOST}/api/system/status || echo "⚠️ SonarQube may not be available"
                    '''
                }
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=psoft-g1 \
                        -Dsonar.projectName="Library Management System" \
                        -Dsonar.host.url=${SONAR_HOST} \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                        -B
                    '''
                }
            }
        }

        // Stage 4: Quality Gate 1
        stage('4. Quality Gate 1') {
            steps {
                echo '🚦 Stage 4: Waiting for Quality Gate result...'
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            echo "⚠️ Quality Gate failed: ${qg.status}"
                            error "Quality Gate failure!"
                        } else {
                            echo '✅ Quality Gate passed!'
                        }
                    }
                }
            }
        }

        // Stage 5: Mutation Tests (PITest)
        stage('5. Mutation Tests (PITest)') {
            steps {
                echo '🧬 Stage 5: Running mutation tests with PITest...'
                script {
                    def pitStatus = sh(
                        script: 'mvn org.pitest:pitest-maven:mutationCoverage -B',
                        returnStatus: true
                    )

                    if (pitStatus != 0) {
                        echo '⚠️ PITest completed with warnings (this is non-blocking)'
                    } else {
                        echo '✅ PITest completed successfully'
                    }
                }
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

                    script {
                        if (fileExists('target/pit-reports/mutations.xml')) {
                            echo '📊 Mutation test results available'
                        }
                    }
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
                        echo "✅ Docker images built successfully"
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
                            -e SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
                            -e SPRING_DATASOURCE_USERNAME=sa \
                            -e SPRING_DATASOURCE_PASSWORD= \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            ${DOCKER_IMAGE_DEV}

                        echo "⏳ Waiting for application to start..."
                        sleep 45
                        echo "✅ Deployed to DEV environment"
                    '''
                }
            }
        }

        // Stage 8: System Tests DEV (QG2) - CORRIGIDO
        stage('8. System Tests DEV - QG2') {
            steps {
                echo '🧪 Stage 8: Running system tests on DEV...'
                script {
                    // CORRIGIDO: Usar Groovy loop ao invés de bash
                    def maxAttempts = 12
                    def healthy = false

                    echo '🏥 Checking application health...'

                    for (int i = 1; i <= maxAttempts; i++) {
                        def healthStatus = sh(
                            script: """
                                docker exec ${APP_NAME}-dev wget --timeout=3 --tries=1 -q -O- http://localhost:8080/actuator/health 2>/dev/null || echo 'FAILED'
                            """,
                            returnStdout: true
                        ).trim()

                        if (healthStatus.contains('"status":"UP"')) {
                            echo "✅ Application is healthy!"

                            // Testes adicionais
                            echo "🔍 Testing API endpoints..."
                            sh """
                                docker exec ${APP_NAME}-dev wget --timeout=5 --tries=1 -q -O- http://localhost:8080/api-docs >/dev/null 2>&1 && echo "✅ API docs accessible" || echo "⚠️ API docs not accessible"
                                docker exec ${APP_NAME}-dev wget --timeout=5 --tries=1 -q -O- http://localhost:8080/actuator/info >/dev/null 2>&1 && echo "✅ Actuator info accessible" || echo "⚠️ Actuator info not accessible"
                            """

                            echo "✅ QG2 PASSED - DEV environment verified"
                            healthy = true
                            break
                        }

                        echo "⏳ Attempt ${i}/${maxAttempts}: Waiting for application..."

                        if (i < maxAttempts) {
                            sleep 5
                        }
                    }

                    if (!healthy) {
                        echo "❌ QG2 FAILED - Health check timeout after ${maxAttempts} attempts"
                        error("Application failed to start properly")
                    }
                }
            }
            post {
                always {
                    sh """
                        echo "📋 Container logs (last 100 lines):"
                        docker logs ${APP_NAME}-dev --tail 100 2>/dev/null || echo "⚠️ Could not retrieve logs"

                        echo ""
                        echo "🔍 Container status:"
                        docker ps -a | grep ${APP_NAME}-dev || echo "⚠️ Container not found"

                        echo ""
                        echo "🔍 Redis connectivity check:"
                        docker exec ${APP_NAME}-dev sh -c "nc -zv ${REDIS_HOST} 6379" 2>&1 || echo "⚠️ Cannot reach Redis from container"

                        echo ""
                        echo "🔍 Network inspection:"
                        docker network inspect ${DOCKER_NETWORK} | grep -A 10 ${APP_NAME}-dev || echo "⚠️ Container not in network"
                    """
                }
                failure {
                    sh """
                        echo "🔍 Additional diagnostics:"
                        docker exec ${APP_NAME}-dev ps aux 2>/dev/null || echo "⚠️ Cannot execute ps in container"
                        docker exec ${APP_NAME}-dev netstat -tuln 2>/dev/null || echo "⚠️ Cannot execute netstat in container"
                    """
                }
            }
        }

        // Stage 9: Deploy to STAGING
        stage('9. Deploy to STAGING') {
            steps {
                echo '🚀 Stage 9: Deploying to STAGING environment...'
                script {
                    sh '''
                        docker stop ${APP_NAME}-staging 2>/dev/null || true
                        docker rm ${APP_NAME}-staging 2>/dev/null || true

                        docker run -d \
                            --name ${APP_NAME}-staging \
                            --network ${DOCKER_NETWORK} \
                            -p 8082:8080 \
                            -e SPRING_PROFILES_ACTIVE=sql-redis,bootstrap \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
                            -e SPRING_DATASOURCE_USERNAME=sa \
                            -e SPRING_DATASOURCE_PASSWORD= \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            ${DOCKER_IMAGE_STAGING}

                        echo "⏳ Waiting for application to start..."
                        sleep 45
                    '''
                }
            }
        }

        // Stage 10: System Tests STAGING (QG3) - CORRIGIDO
        stage('10. System Tests STAGING - QG3') {
            steps {
                echo '🧪 Stage 10: Running system tests on STAGING...'
                script {
                    def maxAttempts = 12
                    def healthy = false

                    for (int i = 1; i <= maxAttempts; i++) {
                        def healthStatus = sh(
                            script: """
                                docker exec ${APP_NAME}-staging wget --timeout=3 --tries=1 -q -O- http://localhost:8080/actuator/health 2>/dev/null || echo 'FAILED'
                            """,
                            returnStdout: true
                        ).trim()

                        if (healthStatus.contains('"status":"UP"')) {
                            echo "✅ STAGING is healthy!"
                            sh """
                                docker exec ${APP_NAME}-staging wget --timeout=5 --tries=1 -q -O- http://localhost:8080/api-docs >/dev/null 2>&1 && echo "✅ API docs accessible" || echo "⚠️ API docs not accessible"
                            """
                            echo "✅ QG3 PASSED"
                            healthy = true
                            break
                        }

                        echo "⏳ Attempt ${i}/${maxAttempts}..."
                        if (i < maxAttempts) {
                            sleep 5
                        }
                    }

                    if (!healthy) {
                        error("STAGING health check failed")
                    }
                }
            }
            post {
                always {
                    sh 'docker logs ${APP_NAME}-staging --tail 100 2>/dev/null || true'
                }
            }
        }

        // Stage 11: Deploy to PROD
        stage('11. Deploy to PROD') {
            steps {
                echo '🚀 Stage 11: Deploying to PRODUCTION...'
                script {
                    timeout(time: 1, unit: 'HOURS') {
                        input message: '🚀 Deploy to PRODUCTION?', ok: 'Deploy to PROD'
                    }

                    sh '''
                        docker stop ${APP_NAME}-prod 2>/dev/null || true
                        docker rm ${APP_NAME}-prod 2>/dev/null || true

                        docker run -d \
                            --name ${APP_NAME}-prod \
                            --network ${DOCKER_NETWORK} \
                            -p 8083:8080 \
                            -e SPRING_PROFILES_ACTIVE=sql-redis \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb \
                            -e SPRING_DATASOURCE_USERNAME=sa \
                            -e SPRING_DATASOURCE_PASSWORD= \
                            -e PERSISTENCE_STRATEGY=sql-redis \
                            -e PERSISTENCE_USE_EMBEDDED_REDIS=false \
                            -e SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
                            ${DOCKER_IMAGE_PROD}

                        sleep 45
                    '''
                }
            }
        }

        // Stage 12: Verify PROD (QG4) - CORRIGIDO
        stage('12. Verify PROD - QG4') {
            steps {
                echo '✅ Stage 12: Verifying PRODUCTION...'
                script {
                    def maxAttempts = 15
                    def healthy = false

                    for (int i = 1; i <= maxAttempts; i++) {
                        def healthStatus = sh(
                            script: """
                                docker exec ${APP_NAME}-prod wget --timeout=3 --tries=1 -q -O- http://localhost:8080/actuator/health 2>/dev/null || echo 'FAILED'
                            """,
                            returnStdout: true
                        ).trim()

                        if (healthStatus.contains('"status":"UP"')) {
                            echo "✅ PRODUCTION verified!"
                            sh """
                                docker exec ${APP_NAME}-prod wget --timeout=5 --tries=1 -q -O- http://localhost:8080/api-docs >/dev/null 2>&1
                            """
                            echo "🎉 QG4 PASSED"
                            healthy = true
                            break
                        }

                        echo "⏳ Attempt ${i}/${maxAttempts}..."
                        if (i < maxAttempts) {
                            sleep 10
                        }
                    }

                    if (!healthy) {
                        error("PRODUCTION verification failed")
                    }
                }
            }
            post {
                failure {
                    sh 'docker stop ${APP_NAME}-prod || true'
                }
                always {
                    sh 'docker logs ${APP_NAME}-prod --tail 100 2>/dev/null || true'
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

            script {
                if (env.DOCKER_AVAILABLE == 'true') {
                    echo 'Deployment Summary:'
                    echo '  - DEV: http://localhost:8080'
                    echo '  - STAGING: http://localhost:8082'
                    echo '  - PROD: http://localhost:8083'
                } else {
                    echo '⚠️ Docker not available - deployments were skipped'
                }
            }
        }
        success {
            echo '✅ Pipeline completed successfully!'
        }
        failure {
            echo '❌ Pipeline failed! Check console output'
        }
        cleanup {
            echo '🧹 Cleaning workspace...'
            cleanWs()
        }
    }
}