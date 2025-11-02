@echo off
echo ========================================
echo TESTING ELASTICSEARCH ENDPOINTS
echo After SecurityConfig Fix
echo ========================================
echo.
echo Waiting 5 seconds to ensure server is ready...
timeout /t 5 >nul
echo.

echo ========================================
echo TEST 1: Books Endpoint (Basic Auth)
echo ========================================
echo Command: curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
echo ----------------------------------------
timeout /t 2 >nul

echo ========================================
echo TEST 2: Books Top5 (Librarian only)
echo ========================================
echo Command: curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
echo.
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
echo.
echo ----------------------------------------
timeout /t 2 >nul

echo ========================================
echo TEST 3: Authors Endpoint (Basic Auth)
echo ========================================
echo Command: curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
echo.
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
echo.
echo ----------------------------------------
timeout /t 2 >nul

echo ========================================
echo TEST 4: Genres Endpoint (Librarian)
echo ========================================
echo Command: curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/genres/top5
echo.
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/genres/top5
echo.
echo ----------------------------------------
timeout /t 2 >nul

echo ========================================
echo TEST 5: HTTP Status Check (Verbose)
echo ========================================
echo Command: curl.exe -I -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
curl.exe -I -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
echo ----------------------------------------

echo.
echo ========================================
echo TESTS COMPLETE
echo ========================================
echo.
echo Expected Results:
echo - All tests should return HTTP 200 OK
echo - JSON data should be returned
echo - No 403 Forbidden errors
echo - No session-related errors
echo.
pause

