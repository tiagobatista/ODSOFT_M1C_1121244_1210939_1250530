@echo off
echo ========================================
echo REDIS CONFIGURATION TEST
echo After Merge - Testing SQL+Redis
echo ========================================
echo.

set LIBRARIAN=maria@gmail.com:Mariaroberta!123
set READER=manuel@gmail.com:Manuelino123!
set ADMIN=admin@gmail.com:AdminPwd1

echo Testing with Librarian credentials...
echo Configuration: SQL + Redis caching
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

echo [Test 4] Search Authors (to check SQL+Redis data)
echo URL: GET /api/authors?name=a
curl -u %LIBRARIAN% "http://localhost:8080/api/authors?name=a"
echo.
echo.

echo [Test 5] Search Books (to check SQL+Redis data)
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
echo Redis Cache Test - Second Call
echo ========================================
echo Running Genre Top5 again to test cache hit...
echo.
curl -u %LIBRARIAN% http://localhost:8080/api/genres/top5
echo.
echo.

echo ========================================
echo Tests Complete!
echo ========================================
echo.
echo Expected Results:
echo - All data should match Elasticsearch results
echo - Same 6 authors, 6 books, 4 genres
echo - Second Genre Top5 call should be faster (cache hit)
echo - Data now stored in SQL with Redis caching
echo.
pause

