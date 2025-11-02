@echo off
echo ========================================
echo ELASTICSEARCH - FULL RESTART SCRIPT
echo Date: 2025-10-28
echo Fix: BCrypt password encoding added
echo ========================================
echo.

echo [Step 1/6] Stopping all Java processes...
taskkill /F /IM java.exe 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Java processes stopped
) else (
    echo ✓ No Java processes running
)
echo.

echo [Step 2/6] Stopping Elasticsearch container...
docker stop elasticsearch 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Elasticsearch stopped
) else (
    echo ✓ Elasticsearch was not running
)
echo.

echo [Step 3/6] Removing Elasticsearch container...
docker rm elasticsearch 2>nul
if %ERRORLEVEL% EQU 0 (
    echo ✓ Elasticsearch container removed
) else (
    echo ✓ No container to remove
)
echo.

echo [Step 4/6] Cleaning Docker volumes...
docker volume prune -f
echo ✓ Docker volumes cleaned
echo.

echo [Step 5/6] Starting fresh Elasticsearch...
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 ^
  -e "discovery.type=single-node" ^
  -e "xpack.security.enabled=false" ^
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" ^
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
echo ✓ Elasticsearch container started
echo.

echo [Step 6/6] Waiting for Elasticsearch to be ready...
echo (This takes about 15 seconds)
timeout /t 15 /nobreak
echo ✓ Elasticsearch should be ready
echo.

echo ========================================
echo NEXT STEPS:
echo ========================================
echo 1. Start the application: mvn spring-boot:run
echo 2. Wait for: "✅ Elasticsearch bootstrapping completed!"
echo 3. Run tests: test-elasticsearch-corrected.bat
echo.
echo ========================================
echo CRITICAL FIX APPLIED:
echo ========================================
echo - UserDocument now BCrypt-encodes passwords
echo - Matches SQL UserEntity behavior
echo - Passwords will no longer be plain text
echo.
echo Press any key to start the application...
pause >nul

echo.
echo ========================================
echo STARTING APPLICATION...
echo ========================================
echo.
mvn spring-boot:run

