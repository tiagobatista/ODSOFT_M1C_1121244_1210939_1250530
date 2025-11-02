@echo off
echo ========================================
echo ELASTICSEARCH ENDPOINT TESTS (CORRECTED)
echo Date: 2025-10-28
echo ========================================
echo.
echo Waiting 5 seconds for server readiness...
timeout /t 5 /nobreak >nul
echo.

echo ========================================
echo TEST GROUP 1: Books Search (POST /api/books/search)
echo ========================================
echo.

echo [Test 1.1] Maria - POST /api/books/search (all books)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -X POST http://localhost:8080/api/books/search ^
  -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"page\":{\"pageNumber\":1,\"pageSize\":10},\"query\":{}}"

echo [Test 1.2] Manuel - POST /api/books/search (should also work for READER)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -X POST http://localhost:8080/api/books/search ^
  -u manuel@gmail.com:Manuelino123! ^
  -H "Content-Type: application/json" ^
  -d "{\"page\":{\"pageNumber\":1,\"pageSize\":10},\"query\":{}}"

echo [Test 1.3] Admin - POST /api/books/search
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -X POST http://localhost:8080/api/books/search ^
  -u admin@gmail.com:AdminPwd1 ^
  -H "Content-Type: application/json" ^
  -d "{\"page\":{\"pageNumber\":1,\"pageSize\":10},\"query\":{}}"

echo ========================================
echo TEST GROUP 2: Authors Search (GET /api/authors?name=...)
echo ========================================
echo.

echo [Test 2.1] Maria - GET /api/authors?name=Pina
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u maria@gmail.com:Mariaroberta!123 ^
  "http://localhost:8080/api/authors?name=Pina"

echo [Test 2.2] Manuel - GET /api/authors?name=Alexandre
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u manuel@gmail.com:Manuelino123! ^
  "http://localhost:8080/api/authors?name=Alexandre"

echo [Test 2.3] Admin - GET /api/authors?name=Manuel
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u admin@gmail.com:AdminPwd1 ^
  "http://localhost:8080/api/authors?name=Manuel"

echo [Test 2.4] Maria - GET /api/authors?name= (empty search)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200 or 400\n\n" ^
  -u maria@gmail.com:Mariaroberta!123 ^
  "http://localhost:8080/api/authors?name="

echo ========================================
echo TEST GROUP 3: Admin Authentication Specific
echo ========================================
echo.

echo [Test 3.1] Admin - GET /api/books/top5
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u admin@gmail.com:AdminPwd1 ^
  http://localhost:8080/api/books/top5

echo [Test 3.2] Admin - GET /api/authors/top5
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u admin@gmail.com:AdminPwd1 ^
  http://localhost:8080/api/authors/top5

echo [Test 3.3] Admin with wrong password (should fail)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 401\n\n" ^
  -u admin@gmail.com:WRONG_PASSWORD ^
  http://localhost:8080/api/books/top5

echo ========================================
echo TEST GROUP 4: Known Working Endpoints
echo ========================================
echo.

echo [Test 4.1] Maria - GET /api/books/top5 (CONFIRMED WORKING)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u maria@gmail.com:Mariaroberta!123 ^
  http://localhost:8080/api/books/top5

echo [Test 4.2] Manuel - GET /api/authors/top5
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u manuel@gmail.com:Manuelino123! ^
  http://localhost:8080/api/authors/top5

echo [Test 4.3] Maria - GET /api/genres/top5 (LIBRARIAN only)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 200\n\n" ^
  -u maria@gmail.com:Mariaroberta!123 ^
  http://localhost:8080/api/genres/top5

echo [Test 4.4] Manuel - GET /api/genres/top5 (should fail - READER)
curl -s -o nul -w "HTTP Status: %%{http_code}\nExpected: 403\n\n" ^
  -u manuel@gmail.com:Manuelino123! ^
  http://localhost:8080/api/genres/top5

echo ========================================
echo TEST GROUP 5: Verbose Admin Debug
echo ========================================
echo.
echo [Test 5.1] Admin verbose curl for debugging
echo This will show full request/response headers...
echo.
curl -v -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
echo.
echo.

echo ========================================
echo SUMMARY
echo ========================================
echo.
echo Review the HTTP status codes above:
echo - 200 = Success
echo - 401 = Authentication failed (wrong user/password)
echo - 403 = Authorized but not permitted (wrong role)
echo - 404 = Endpoint not found (check URL/method)
echo - 400 = Bad request (missing parameter)
echo.
echo If you see unexpected results:
echo 1. Check which profile is active (elasticsearch or sql-redis)
echo 2. Verify Elasticsearch is running: docker ps
echo 3. Check application logs for bootstrap messages
echo 4. Verify data in Elasticsearch: curl http://localhost:9200/users/_search?pretty
echo.
pause

