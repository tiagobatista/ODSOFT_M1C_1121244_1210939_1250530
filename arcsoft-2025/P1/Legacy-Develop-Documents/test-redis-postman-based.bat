
@echo off
chcp 65001 >nul
echo ========================================
echo REDIS CACHING TEST
echo Based on Postman Collection
echo ========================================
echo.

REM Configuration from Postman environment
set HOST=http://localhost:8080
set BASE_URL=/api
set MARIA_USER=maria@gmail.com
set MARIA_PASS=Mariaroberta!123
set MANUEL_USER=manuel@gmail.com
set MANUEL_PASS=Manuelino123!

echo Configuration:
echo - Host: %HOST%
echo - Base URL: %BASE_URL%
echo - Test User (Librarian): %MARIA_USER%
echo - Test User (Reader): %MANUEL_USER%
echo.

echo Checking application status...
netstat -ano | findstr :8080 >nul
if errorlevel 1 (
    echo ❌ ERROR: Application not running on port 8080
    echo Please start with: mvn spring-boot:run
    pause
    exit /b 1
)
echo ✓ Application running
echo.

netstat -ano | findstr :6379 >nul
if errorlevel 1 (
    echo ⚠️  WARNING: Redis might not be running
) else (
    echo ✓ Redis running on port 6379
)
echo.
pause

echo ========================================
echo TEST 1: Login (Authentication)
echo ========================================
echo.
echo [1.1] Login as Librarian (Maria)...

curl.exe -s -X POST -H "Content-Type: application/json" -d "{\"username\":\"%MARIA_USER%\",\"password\":\"%MARIA_PASS%\"}" %HOST%%BASE_URL%/public/login -D maria_headers.txt
echo.
echo ✓ Logged in as Maria
echo.

echo [1.2] Login as Reader (Manuel)...
curl.exe -s -X POST -H "Content-Type: application/json" -d "{\"username\":\"%MANUEL_USER%\",\"password\":\"%MANUEL_PASS%\"}" %HOST%%BASE_URL%/public/login -D manuel_headers.txt
echo.
echo ✓ Logged in as Manuel
echo.
pause

echo ========================================
echo TEST 2: Books Top 5 - REDIS CACHE TEST
echo ========================================
echo.
echo This endpoint should demonstrate Redis caching.
echo The first call fetches from SQL, subsequent calls from Redis.
echo.

echo [2.1] First call to /books/top5 (CACHE MISS)
echo 📖 Expected: Data fetched from SQL database
echo ⏱️  Measuring response time...
echo.
curl.exe -w "\n⏱️ Response time: %%{time_total}s\nHTTP Status: %%{http_code}\n" -u %MARIA_USER%:%MARIA_PASS% %HOST%%BASE_URL%/books/top5 > top5_result1.json
type top5_result1.json
echo.
echo 👀 CHECK APPLICATION CONSOLE for: "📖 CACHE MISS - Fetching from SQL"
echo.
timeout /t 3 /nobreak >nul

echo [2.2] Second call to /books/top5 (CACHE HIT)
echo 📚 Expected: Data fetched from Redis cache
echo ⏱️  Should be faster than first call...
echo.
curl.exe -w "\n⏱️ Response time: %%{time_total}s\nHTTP Status: %%{http_code}\n" -u %MARIA_USER%:%MARIA_PASS% %HOST%%BASE_URL%/books/top5 > top5_result2.json
type top5_result2.json
echo.
echo 👀 CHECK APPLICATION CONSOLE for: "📚 CACHE HIT"
echo.
timeout /t 2 /nobreak >nul

echo [2.3] Third call to /books/top5 (Still CACHE HIT)
echo.
curl.exe -w "\n⏱️ Response time: %%{time_total}s\nHTTP Status: %%{http_code}\n" -u %MARIA_USER%:%MARIA_PASS% %HOST%%BASE_URL%/books/top5 > top5_result3.json
type top5_result3.json
echo.
pause

echo ========================================
echo TEST 3: Authors Top 5 - REDIS CACHE TEST
echo ========================================
echo.

echo [3.1] First call to /authors/top5 (CACHE MISS)
curl.exe -w "\n⏱️ Time: %%{time_total}s | HTTP: %%{http_code}\n" -u %MANUEL_USER%:%MANUEL_PASS% %HOST%%BASE_URL%/authors/top5
echo.
timeout /t 2 /nobreak >nul

echo [3.2] Second call to /authors/top5 (CACHE HIT)
curl.exe -w "\n⏱️ Time: %%{time_total}s | HTTP: %%{http_code}\n" -u %MANUEL_USER%:%MANUEL_PASS% %HOST%%BASE_URL%/authors/top5
echo.
pause

echo ========================================
echo TEST 4: Individual Book Fetch
echo ========================================
echo.
echo If books exist, this will test individual book caching...
echo.

echo [4.1] Get book by ISBN - First time (CACHE MISS)
curl.exe -w "\n⏱️ Time: %%{time_total}s\n" -u %MARIA_USER%:%MARIA_PASS% %HOST%%BASE_URL%/books/9789723716160
echo.
timeout /t 2 /nobreak >nul

echo [4.2] Get same book - Second time (CACHE HIT)
curl.exe -w "\n⏱️ Time: %%{time_total}s\n" -u %MARIA_USER%:%MARIA_PASS% %HOST%%BASE_URL%/books/9789723716160
echo.
pause

echo ========================================
echo TEST 5: Search Books (if available)
echo ========================================
echo.

echo [5.1] Search books - First time
curl.exe -w "\nHTTP: %%{http_code}\n" -u %MARIA_USER%:%MARIA_PASS% "%HOST%%BASE_URL%/books?page=0&size=5"
echo.
timeout /t 2 /nobreak >nul

echo [5.2] Search books - Second time (potential cache)
curl.exe -w "\nHTTP: %%{http_code}\n" -u %MARIA_USER%:%MARIA_PASS% "%HOST%%BASE_URL%/books?page=0&size=5"
echo.

echo ========================================
echo CLEANUP
echo ========================================
del maria_headers.txt manuel_headers.txt top5_result1.json top5_result2.json top5_result3.json 2>nul
echo.

echo ========================================
echo TEST SUMMARY
echo ========================================
echo.
echo ✅ Redis Caching Tests Completed!
echo.
echo What to verify:
echo.
echo 1. In APPLICATION CONSOLE:
echo    - Look for "📖 CACHE MISS - Fetching from SQL" messages
echo    - Look for "📚 CACHE HIT" messages on subsequent calls
echo    - Look for "♻️ Updated Redis cache" messages
echo.
echo 2. Response Times:
echo    - First calls should be slower (SQL database access)
echo    - Cached calls should be faster (Redis memory access)
echo.
echo 3. Data Consistency:
echo    - Same data should be returned for cache hits
echo.
echo Redis Cache Keys (based on implementation):
echo    - books:isbn:{isbn} - TTL: 30 minutes
echo    - authors:id:{id} - TTL: 30 minutes
echo    - books:top5 - TTL: 10 minutes
echo    - authors:top5 - TTL: 10 minutes
echo.
pause

