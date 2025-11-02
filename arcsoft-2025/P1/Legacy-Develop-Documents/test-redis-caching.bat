@echo off
echo ========================================
echo REDIS CACHING TEST
echo ========================================
echo.
echo This test will demonstrate Redis caching by:
echo 1. First request (CACHE MISS - fetch from SQL)
echo 2. Second request (CACHE HIT - fetch from Redis)
echo 3. Check application logs for cache indicators
echo.
pause

echo.
echo ========================================
echo TEST 1: Fetch Book by ISBN (First Time)
echo Expected: CACHE MISS
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
echo.

echo ========================================
echo TEST 2: Fetch Same Book Again
echo Expected: CACHE HIT (faster response)
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
echo.

echo ========================================
echo TEST 3: Fetch Different Book (First Time)
echo Expected: CACHE MISS
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789895612864
echo.
echo.

echo ========================================
echo TEST 4: Fetch Book Again
echo Expected: CACHE HIT
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789895612864
echo.
echo.

echo ========================================
echo TEST 5: Top 5 Books (First Time)
echo Expected: CACHE MISS
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo.
echo.

echo ========================================
echo TEST 6: Top 5 Books (Second Time)
echo Expected: CACHE HIT
echo ========================================
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo.
echo.

echo ========================================
echo REDIS CACHE TEST COMPLETE
echo ========================================
echo.
echo Check the application console for cache indicators:
echo - Look for "CACHE MISS" messages
echo - Look for "CACHE HIT" messages
echo - Compare response times (cached should be faster)
echo.
pause

