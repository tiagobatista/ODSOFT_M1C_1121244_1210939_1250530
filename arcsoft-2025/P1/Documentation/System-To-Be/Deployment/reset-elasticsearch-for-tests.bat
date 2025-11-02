@echo off
REM ============================================
REM  Elasticsearch Clean Reset for Testing
REM  Location: arcsoft-2025/P1/Documentation/System-To-Be/Deployment/
REM ============================================
REM
REM This script resets the Elasticsearch container to ensure clean
REM bootstrap data for each functional test run.
REM
REM Usage (from project root):
REM   arcsoft-2025\P1\Documentation\System-To-Be\Deployment\reset-elasticsearch-for-tests.bat
REM
REM What it does:
REM   1. Stops and removes old Elasticsearch container
REM   2. Prunes Docker volumes (removes old data)
REM   3. Starts fresh Elasticsearch container
REM   4. Waits for initialization
REM   5. Verifies Elasticsearch is ready
REM
REM After running this script:
REM   - Start app: mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
REM   - Run tests: mvn test -Dtest=ElasticsearchSystemTest
REM ============================================

echo.
echo ============================================
echo  Elasticsearch Clean Reset for Testing
echo ============================================
echo.

echo [1/5] Stopping old Elasticsearch container...
docker stop elasticsearch 2>nul
docker rm elasticsearch 2>nul
echo       Done.

echo.
echo [2/5] Pruning Docker volumes (removes old data)...
docker volume prune -f >nul 2>&1
echo       Done.

echo.
echo [3/5] Starting fresh Elasticsearch container...
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
echo       Done.

echo.
echo [4/5] Waiting 45 seconds for Elasticsearch to initialize...
timeout /t 45 /nobreak >nul
echo       Done.

echo.
echo [5/5] Verifying Elasticsearch is responding...
curl.exe http://localhost:9200 2>nul | findstr "elasticsearch" >nul
if %errorlevel% equ 0 (
    echo       ✓ Elasticsearch is ready!
) else (
    echo       ⚠ Warning: Elasticsearch may not be fully ready yet
    echo       Try waiting another 15 seconds and test manually:
    echo       curl.exe http://localhost:9200
)

echo.
echo ============================================
echo  Setup Complete!
echo ============================================
echo.
echo Next steps:
echo   1. Start application with bootstrap:
echo      mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
echo.
echo   2. Wait for "Started PsoftG1Application" message
echo.
echo   3. In a NEW terminal window, run tests:
echo      mvn test -Dtest=ElasticsearchSystemTest
echo.
echo ============================================
pause

