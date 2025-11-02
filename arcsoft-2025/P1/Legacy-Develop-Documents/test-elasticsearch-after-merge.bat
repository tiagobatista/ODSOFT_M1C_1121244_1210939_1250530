@echo off
echo ========================================
echo ELASTICSEARCH ENDPOINTS TEST
echo After Merge - Comparing Results
echo ========================================
echo.

set LIBRARIAN=maria@gmail.com:Mariaroberta!123
set READER=manuel@gmail.com:Manuelino123!
set ADMIN=admin@gmail.com:AdminPwd1

echo Testing with Librarian credentials...
echo.

echo [Test 1] Genre Top5
echo URL: GET /api/genres/top5
curl -u %LIBRARIAN% http://localhost:8080/api/genres/top5
echo.
echo.

echo [Test 2] Book Top5
echo URL: GET /api/books/top5
curl -u %LIBRARIAN% http://localhost:8080/api/books/top5
echo.
echo.

echo [Test 3] Author Top5
echo URL: GET /api/authors/top5
curl -u %LIBRARIAN% http://localhost:8080/api/authors/top5
echo.
echo.

echo [Test 4] Search Authors (to check Elasticsearch data)
echo URL: GET /api/authors?name=a
curl -u %LIBRARIAN% "http://localhost:8080/api/authors?name=a"
echo.
echo.

echo [Test 5] Search Books (to check Elasticsearch data)
echo URL: POST /api/books/search
curl -u %LIBRARIAN% -X POST -H "Content-Type: application/json" -d "{\"title\":\"\"}" http://localhost:8080/api/books/search
echo.
echo.

echo [Test 6] ISBN Service (New Feature from Master)
echo URL: GET /api/isbn/providers
curl http://localhost:8080/api/isbn/providers
echo.
echo.

echo ========================================
echo Tests Complete!
echo ========================================
echo.
echo Compare these results with previous test results
echo to ensure the merge didn't break anything.
echo.
echo Expected:
echo - Genre Top5: Real counts from Elasticsearch
echo - Book Top5: 5 books with simulated lending counts
echo - Author Top5: 5 authors with simulated lending counts
echo - All data should be from Elasticsearch
echo - ISBN providers should list available APIs
echo.
pause

