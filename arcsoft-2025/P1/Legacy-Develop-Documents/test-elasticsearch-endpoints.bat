@echo off
REM Elasticsearch API Endpoint Tests
echo ========================================
echo Testing Elasticsearch Endpoints
echo ========================================
echo.

echo Test 1: GET /api/books (List all books)
echo ----------------------------------------
curl.exe -s -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
echo.

echo Test 2: GET /api/books/top5 (Top 5 books)
echo ----------------------------------------
curl.exe -s -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
echo.
echo.

echo Test 3: GET /api/authors (List all authors)
echo ----------------------------------------
curl.exe -s -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/authors
echo.
echo.

echo Test 4: GET /api/genres (List all genres)
echo ----------------------------------------
curl.exe -s -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/genres
echo.
echo.

echo Test 5: Search books by title
echo ----------------------------------------
curl.exe -s -u admin@gmail.com:AdminPwd1 "http://localhost:8080/api/books?title=Casa"
echo.
echo.

echo ========================================
echo Tests Complete
echo ========================================

