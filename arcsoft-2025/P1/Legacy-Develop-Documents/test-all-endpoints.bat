@echo off
echo ========================================
echo COMPREHENSIVE ENDPOINT DIAGNOSTIC
echo ========================================
echo.
echo Waiting 5 seconds for server readiness...
timeout /t 5 >nul
echo.

echo ========================================
echo TEST GROUP 1: Admin (LIBRARIAN)
echo ========================================
echo.

echo [Test 1.1] Admin - GET /api/books
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo Expected: 200
echo.

echo [Test 1.2] Admin - GET /api/books/top5
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
echo Expected: 200
echo.

echo [Test 1.3] Admin - GET /api/authors
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/authors
echo Expected: 200
echo.

echo ========================================
echo TEST GROUP 2: Maria (LIBRARIAN)
echo ========================================
echo.

echo [Test 2.1] Maria - GET /api/books
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
echo Expected: 200
echo.

echo [Test 2.2] Maria - GET /api/books/top5 (CONFIRMED WORKING)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
echo Expected: 200
echo.

echo [Test 2.3] Maria - GET /api/authors
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/authors
echo Expected: 200
echo.

echo [Test 2.4] Maria - GET /api/books/suggestions (SHOULD FAIL - READER only)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/suggestions
echo Expected: 403
echo.

echo ========================================
echo TEST GROUP 3: Manuel (READER)
echo ========================================
echo.

echo [Test 3.1] Manuel - GET /api/books
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
echo Expected: 200
echo.

echo [Test 3.2] Manuel - GET /api/authors (REPORTED AS FAILING)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
echo Expected: 200
echo.

echo [Test 3.3] Manuel - GET /api/authors/top5
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
echo Expected: 200
echo.

echo [Test 3.4] Manuel - GET /api/books/top5 (SHOULD FAIL - LIBRARIAN only)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5
echo Expected: 403
echo.

echo [Test 3.5] Manuel - GET /api/genres/top5 (SHOULD FAIL - LIBRARIAN only)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/genres/top5
echo Expected: 403
echo.

echo ========================================
echo TEST GROUP 4: Wrong Credentials
echo ========================================
echo.

echo [Test 4.1] Wrong password (SHOULD FAIL)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u admin@gmail.com:WRONGPASSWORD http://localhost:8080/api/books
echo Expected: 401
echo.

echo [Test 4.2] Non-existent user (SHOULD FAIL)
curl.exe -s -o nul -w "HTTP Status: %%{http_code}\n" -u fake@gmail.com:FakePassword123 http://localhost:8080/api/books
echo Expected: 401
echo.

echo ========================================
echo SUMMARY
echo ========================================
echo.
echo Review the HTTP status codes above:
echo - 200 = Success
echo - 401 = Authentication failed (wrong user/password)
echo - 403 = Authorized but not permitted (wrong role)
echo.
echo If you see mostly 401 errors:
echo   - Application might not be fully started
echo   - Users might not be created in database
echo   - Check application logs
echo.
echo If you see 403 where you expect 200:
echo   - Check user role (READER vs LIBRARIAN)
echo   - Check SecurityConfig authorization rules
echo.
pause

