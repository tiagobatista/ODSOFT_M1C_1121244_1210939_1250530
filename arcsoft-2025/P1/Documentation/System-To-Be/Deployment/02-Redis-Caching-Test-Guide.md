# Redis Caching - Testing Guide

## Overview

This guide demonstrates how to test Redis caching functionality in the Library Management System running with the SQL + Redis profile.

---

## Prerequisites

✅ **Redis container running:**
```cmd
docker run -d --name redis -p 6379:6379 redis:latest
```

✅ **Application running with SQL + Redis profile:**

**PowerShell:**
```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**CMD:**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**Note:** Keep this terminal window open - you'll need it to stop the app later.

✅ **Verify services are running:**
```cmd
REM Check if application is running
netstat -ano | findstr :8080

REM Check if Redis is running
netstat -ano | findstr :6379
```

---

## How to Stop the Application

**Option 1: Graceful shutdown (RECOMMENDED)**
- Go to the terminal where Maven is running
- Press `Ctrl+C`
- Wait for the application to shut down

**Option 2: Force stop**
```cmd
REM Find the Java process
netstat -ano | findstr :8080

REM Kill by process ID (replace <PID> with actual number)
taskkill /F /PID <PID>

REM Or kill all Java processes
taskkill /F /IM java.exe
```

---

## Test Credentials

The bootstrap profile creates the following test users:

### Readers
- **Email:** `manuel@gmail.com` | **Password:** `Manuelino123!`
- **Email:** `joao@gmail.com` | **Password:** `Joaozinho!123`

### Librarians
- **Email:** `maria@gmail.com` | **Password:** `Mariaroberta!123`
- **Email:** `admin@gmail.com` | **Password:** `AdminPwd1`

---

## Test 1: Cache Verification - Book by ISBN

### Step 1: First Request (CACHE MISS)

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

**Expected Behavior:**
- ✅ Returns book data
- 📝 **Check application console:** You should see log message indicating "CACHE MISS" or "Fetching from database"
- ⏱️ Response time: ~100-300ms (depending on your system)

### Step 2: Second Request (CACHE HIT)

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

**Expected Behavior:**
- ✅ Returns same book data
- 📝 **Check application console:** You should see log message indicating "CACHE HIT" or "Fetching from Redis cache"
- ⏱️ Response time: ~10-50ms (significantly faster)

### Step 3: Measure Response Time

```cmd
curl.exe -w "\nTime: %{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

**Expected:**
- First call: Slower (database query)
- Subsequent calls: Faster (Redis cache)

---

## Test 2: List All Books (Paginated)

### First Request (CACHE MISS)

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?page=0&size=5"
```

**Expected:**
- ✅ Returns 5 books
- 📝 Console shows database query

### Second Request (CACHE HIT)

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?page=0&size=5"
```

**Expected:**
- ✅ Returns same 5 books (faster)
- 📝 Console shows cache hit

---

## Test 3: Top 5 Authors

### First Request (CACHE MISS)

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

**Expected:**
- ✅ Returns top 5 authors with lending counts
- 📝 Console shows database query execution

### Second Request (CACHE HIT)

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

**Expected:**
- ✅ Returns same data (faster)
- 📝 Console shows Redis cache retrieval

---

## Test 4: Verify Cache in Redis CLI

### Step 1: Connect to Redis Container

```cmd
docker exec -it redis redis-cli
```

### Step 2: View All Cached Keys

```redis
KEYS *
```

**Expected Output Example:**
```
1) "books::9789723716160"
2) "books::top5"
3) "authors::top5"
4) "books::SimpleKey []"
```

### Step 3: Inspect Specific Cache Entry

```redis
GET "books::9789723716160"
```

**Expected:**
- Shows serialized book data (may be in binary format)

### Step 4: Check Cache TTL (Time To Live)

```redis
TTL "books::top5"
```

**Expected:**
- Returns remaining seconds until cache expiration (e.g., `86400` for 24 hours)
- Returns `-1` if no expiration set

### Step 5: Clear Specific Cache

```redis
DEL "books::9789723716160"
```

**Then test again:**
```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

Should result in CACHE MISS again.

### Step 6: Clear All Cache

```redis
FLUSHALL
```

**WARNING:** This clears ALL Redis data!

### Step 7: Exit Redis CLI

```redis
EXIT
```

---

## Test 5: Cache Invalidation (Update Book)

### Step 1: Get Book Before Update

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

### Step 2: Update Book (Requires LIBRARIAN role)

```cmd
curl.exe -X PATCH -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"title\": \"Updated Title Test\"}" ^
  http://localhost:8080/api/books/9789723716160
```

**Expected:**
- ✅ Book updated successfully
- 📝 Cache for this book should be invalidated automatically

### Step 3: Get Book After Update

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

**Expected:**
- ✅ Returns updated book data
- 📝 Console shows CACHE MISS (cache was invalidated)

### Step 4: Get Book Again

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

**Expected:**
- ✅ Returns updated book data
- 📝 Console shows CACHE HIT (new cache entry created)

---

## Quick Test Script

Save this as `test-redis-quick.bat`:

```bat
@echo off
echo ========================================
echo QUICK REDIS CACHE TEST
echo ========================================
echo.

echo [1] First request - CACHE MISS (slower)
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
timeout /t 2 /nobreak >nul

echo [2] Second request - CACHE HIT (faster)
curl.exe -w "\nTime: %%{time_total}s\n" -s -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
echo.
timeout /t 2 /nobreak >nul

echo [3] Authors Top5 - CACHE MISS
curl.exe -s -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
echo.
timeout /t 2 /nobreak >nul

echo [4] Authors Top5 - CACHE HIT
curl.exe -s -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
echo.

echo ========================================
echo DONE - Check application console for cache logs
echo ========================================
pause
```

---

## Troubleshooting

### Issue: All requests show CACHE MISS

**Possible Causes:**
1. Redis not running
2. Caching disabled in configuration
3. Cache TTL expired

**Solution:**
```cmd
REM Verify Redis is running
docker ps | findstr redis

REM Check Redis connection
docker exec -it redis redis-cli ping
REM Expected: PONG

REM Check cache keys exist
docker exec -it redis redis-cli KEYS "*"
```

### Issue: Authentication failures (403 Forbidden)

**Cause:** Using wrong credentials or role doesn't have permission

**Solution:**
- Use **READER** credentials for read operations:
  - `manuel@gmail.com:Manuelino123!`
- Use **LIBRARIAN** credentials for admin operations:
  - `maria@gmail.com:Mariaroberta!123`

### Issue: Empty response from endpoints

**Possible Causes:**
1. Bootstrap data not created
2. Wrong profile active

**Solution:**
```cmd
REM Check if bootstrap profile is active (should see in startup logs)
REM Restart application with correct profiles
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

### Issue: "Connection refused" to Redis

**Solution:**
```cmd
REM Start Redis if not running
docker start redis

REM Or create new Redis container
docker run -d --name redis -p 6379:6379 redis:latest
```

---

## Monitoring Cache Performance

### Application Logs to Watch

Look for these log messages in the application console:

```
✅ CACHE MISS: Fetching book from database
✅ CACHE HIT: Returning book from Redis cache
✅ CACHE EVICT: Invalidating cache for book ISBN: 9789723716160
```

### Redis Monitoring

```cmd
REM Real-time Redis commands monitoring
docker exec -it redis redis-cli MONITOR

REM View Redis info
docker exec -it redis redis-cli INFO

REM Check memory usage
docker exec -it redis redis-cli INFO memory
```

---

## Expected Performance Improvements

| Operation | Without Cache | With Cache | Improvement |
|-----------|---------------|------------|-------------|
| Get Book by ISBN | ~150ms | ~20ms | **7.5x faster** |
| List Books (paginated) | ~200ms | ~30ms | **6.7x faster** |
| Top 5 Authors | ~300ms | ~25ms | **12x faster** |
| Top 5 Books | ~250ms | ~25ms | **10x faster** |

*Note: Actual times depend on your system performance*

---

## Summary

✅ **Redis caching is working** if:
- First request is slower (database query)
- Subsequent requests are faster (Redis retrieval)
- Console shows CACHE MISS → CACHE HIT pattern
- `KEYS *` in Redis shows cached entries

✅ **Cache invalidation is working** if:
- Updating a resource invalidates its cache
- Next request shows CACHE MISS
- Subsequent requests show CACHE HIT with new data

✅ **Authentication is working** if:
- Correct credentials return data
- Wrong credentials return 401 Unauthorized
- Insufficient permissions return 403 Forbidden

---

## Next Steps

- **Elasticsearch Testing:** See `03-Elasticsearch-Test-Guide.md`
- **Full API Reference:** See `04-API-Endpoints-Reference.md`
- **Performance Benchmarking:** See `05-Performance-Testing-Guide.md`

