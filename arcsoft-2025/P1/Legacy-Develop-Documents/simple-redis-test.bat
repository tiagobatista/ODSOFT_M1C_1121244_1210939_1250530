@echo off
chcp 65001 >nul
echo ========================================
echo SIMPLE REDIS CACHE TEST
echo ========================================
echo.

echo This test will:
echo 1. Create a test book
echo 2. Fetch it twice to demonstrate caching
echo 3. Monitor response times
echo.
pause

echo ========================================
echo Step 1: Creating Test Book
echo ========================================
curl.exe -X POST -H "Content-Type: application/json" -u maria@gmail.com:Mariaroberta!123 -d "{\"title\":\"Redis Cache Test\",\"isbn\":\"9999999999999\",\"genre\":\"Technology\",\"description\":\"Testing Redis caching\",\"authors\":[{\"name\":\"Test Author\",\"bio\":\"Test bio\"}]}" http://localhost:8080/api/books
echo.
echo.
timeout /t 2 /nobreak >nul

echo ========================================
echo Step 2: First Fetch (CACHE MISS)
echo ========================================
echo Expected: Fetch from SQL database
echo.
curl.exe -w "\nResponse Time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9999999999999
echo.
echo 👀 Check application logs for: CACHE MISS
echo.
timeout /t 3 /nobreak >nul

echo ========================================
echo Step 3: Second Fetch (CACHE HIT)
echo ========================================
echo Expected: Fetch from Redis cache (faster)
echo.
curl.exe -w "\nResponse Time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9999999999999
echo.
echo 👀 Check application logs for: CACHE HIT
echo.
echo.

echo ========================================
echo Step 4: Third Fetch (Still CACHE HIT)
echo ========================================
curl.exe -w "\nResponse Time: %%{time_total}s\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9999999999999
echo.
echo.

echo ========================================
echo TEST COMPLETE
echo ========================================
echo.
echo Compare the response times above:
echo - First request should be slower (SQL fetch)
echo - Second and third should be faster (Redis cache)
echo.
echo Check the application console for cache log messages:
echo   📖 CACHE MISS - Fetching from SQL
echo   📚 CACHE HIT - Fetched from Redis
echo   ♻️ Updated Redis cache
echo.
pause

