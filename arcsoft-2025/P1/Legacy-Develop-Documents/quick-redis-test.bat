@echo off
echo Testing Redis Caching - Simple Version
echo ========================================
echo.

echo Test 1: First call to books/top5 (CACHE MISS)
echo -----------------------------------------------
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo.
echo.
echo ** Check console for CACHE MISS **
timeout /t 3 /nobreak

echo.
echo Test 2: Second call to books/top5 (CACHE HIT)
echo -----------------------------------------------
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo.
echo.
echo ** Check console for CACHE HIT (should be faster) **
timeout /t 3 /nobreak

echo.
echo Test 3: Third call (Still CACHE HIT)
echo -----------------------------------------------
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo.
echo.

echo ========================================
echo DONE - Check the application console for:
echo   - CACHE MISS messages (first call)
echo   - CACHE HIT messages (subsequent calls)
echo ========================================
pause

