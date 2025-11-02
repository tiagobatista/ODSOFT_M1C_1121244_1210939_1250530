@echo off
chcp 65001 >nul
echo ========================================
echo REDIS CACHING DEMONSTRATION
echo SQL + Redis Profile
echo ========================================
echo.

echo Step 1: Verify Application and Redis are Running
echo ================================================
netstat -ano | findstr :8080 >nul
if errorlevel 1 (
    echo ❌ ERROR: Application not running on port 8080
    pause
    exit /b 1
)
echo ✓ Application running on port 8080

netstat -ano | findstr :6379 >nul
if errorlevel 1 (
    echo ⚠️  WARNING: Redis might not be on port 6379
) else (
    echo ✓ Redis running on port 6379
)
echo.
pause

echo ========================================
echo Step 2: Check Current Database State
echo ========================================
echo.
echo [2.1] Checking for existing books...
curl.exe -s -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?page=0&size=3"
echo.
echo.

echo [2.2] Checking for existing authors...
curl.exe -s -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/authors?page=0&size=3"
echo.
echo.
pause

echo ========================================
echo Step 3: Create Test Data (if needed)
echo ========================================
echo.
echo Creating a test book...
curl.exe -X POST -H "Content-Type: application/json" -u maria@gmail.com:Mariaroberta!123 -d @test-book.json http://localhost:8080/api/books
echo.
echo.
pause

echo ========================================
echo Step 4: REDIS CACHE TEST - Individual Book Lookup
echo ========================================
echo.

echo [4.1] First request for ISBN 9781234567890
echo 📖 Expected: CACHE MISS (data fetched from SQL)
echo ⏱️  Measure: Response time
echo.
curl.exe -s -w "\n⏱️ Response time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9781234567890
echo.
echo 👀 Check application console for: "CACHE MISS - Fetching from SQL - Book ISBN: 9781234567890"
echo.
timeout /t 3 /nobreak >nul

echo [4.2] Second request for SAME ISBN 9781234567890
echo 📚 Expected: CACHE HIT (data fetched from Redis)
echo ⏱️  Measure: Response time (should be faster)
echo.
curl.exe -s -w "\n⏱️ Response time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9781234567890
echo.
echo 👀 Check application console for: "CACHE HIT - Book ISBN: 9781234567890"
echo.
pause

echo ========================================
echo Step 5: REDIS CACHE TEST - Different Book
echo ========================================
echo.

echo Testing with a different ISBN to show cache isolation...
echo.
echo [5.1] First request for ISBN 9789723716160
curl.exe -s -w "\n⏱️ Time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
timeout /t 2 /nobreak >nul

echo [5.2] Second request for ISBN 9789723716160
curl.exe -s -w "\n⏱️ Time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
pause

echo ========================================
echo Step 6: Summary & Verification
echo ========================================
echo.
echo ✅ Tests completed!
echo.
echo What to verify in the APPLICATION CONSOLE:
echo   1. Look for "📖 CACHE MISS" on first requests
echo   2. Look for "📚 CACHE HIT" on second requests
echo   3. Look for "♻️ Updated Redis cache" messages
echo   4. Compare response times (cached should be faster)
echo.
echo Redis Cache Configuration:
echo   - books:isbn:{isbn} - TTL: 30 minutes
echo   - authors:id:{id} - TTL: 30 minutes
echo   - readers:number:{number} - TTL: 30 minutes
echo.
echo To verify Redis directly (if redis-cli available):
echo   redis-cli KEYS "*"
echo   redis-cli GET "books:isbn:9781234567890"
echo.
pause

