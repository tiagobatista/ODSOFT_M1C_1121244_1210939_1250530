@echo off
chcp 65001 >nul
echo ========================================
echo COMPREHENSIVE REDIS CACHING TEST
echo SQL + Redis Profile
echo ========================================
echo.

echo Checking if application is running...
netstat -ano | findstr :8080 >nul
if errorlevel 1 (
    echo ERROR: Application is not running on port 8080
    echo Please start the application first with: mvn spring-boot:run
    pause
    exit /b 1
)
echo ✓ Application is running
echo.

echo Checking if Redis is running...
netstat -ano | findstr :6379 >nul
if errorlevel 1 (
    echo WARNING: Redis might not be running on port 6379
) else (
    echo ✓ Redis is running
)
echo.

pause

echo ========================================
echo PHASE 1: Verify Bootstrap Data
echo ========================================
echo.

echo [1.1] Check if books exist in database...
curl.exe -s -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?page=0&size=2" > temp_books.json
type temp_books.json
echo.
echo.

echo [1.2] Check if users exist...
echo (Successful authentication means users were created)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
echo.

pause

echo ========================================
echo PHASE 2: REDIS CACHE TEST - Books
echo ========================================
echo.

echo [2.1] First request for ISBN 9789723716160
echo Expected: CACHE MISS (slow - fetch from SQL)
echo Check console for cache log messages...
echo.
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
timeout /t 2 /nobreak >nul
echo.

echo [2.2] Second request for SAME ISBN
echo Expected: CACHE HIT (faster - fetch from Redis)
echo.
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
timeout /t 2 /nobreak >nul
echo.

pause

echo ========================================
echo PHASE 3: REDIS CACHE TEST - Top 5 Books
echo ========================================
echo.

echo [3.1] First Top5 request
echo Expected: CACHE MISS
echo.
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5 > temp_top5_1.json
type temp_top5_1.json
echo.
timeout /t 2 /nobreak >nul
echo.

echo [3.2] Second Top5 request
echo Expected: CACHE HIT (should be faster)
echo.
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5 > temp_top5_2.json
type temp_top5_2.json
echo.

pause

echo ========================================
echo PHASE 4: CACHE INVALIDATION TEST
echo ========================================
echo.

echo This would require creating/updating a book to see cache invalidation
echo For now, check the application console logs for:
echo   - 📖 CACHE MISS messages
echo   - 📚 CACHE HIT messages
echo   - ♻️ Cache update messages
echo.

echo ========================================
echo TEST COMPLETE
echo ========================================
echo.
echo Summary:
echo - Check response times (cached should be faster)
echo - Check application console for cache log messages
echo - Verify same data returned for both requests
echo.

del temp_books.json temp_top5_1.json temp_top5_2.json 2>nul

pause

