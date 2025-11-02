
✅ **Ready for presentation** if:
- At least 4 out of 6 core tests pass
- Can demonstrate book search functionality
- Can demonstrate full-text search advantage
- Can explain architecture differences

---

## Time Management (Today's Delivery)

### Priority 1 (MUST WORK) - 2 hours
- ✅ Elasticsearch container running
- ✅ Application starts with Elasticsearch profile
- ✅ List books works
- ✅ Search books works

### Priority 2 (SHOULD WORK) - 1 hour
- ✅ Top 5 authors works
- ✅ CRUD operations work
- ✅ Authentication works

### Priority 3 (NICE TO HAVE) - 30 min
- ⏭️ Top 5 books (skip if problematic)
- ⏭️ Complex aggregations

### Documentation (CRITICAL) - 2 hours
- ✅ Update report with test results
- ✅ Document working features
- ✅ Note known limitations
- ✅ Include architecture diagrams

**Total:** ~5.5 hours (fits within your 6-hour window)

---

## Report Checklist

Include in your report:

- [x] Architecture overview (SQL vs Elasticsearch)
- [x] Elasticsearch setup instructions
- [x] Test results with screenshots
- [x] Performance comparison
- [x] Working features list
- [x] Known limitations
- [x] Future improvements

---

## Next Steps

- **Complete Report:** `arcsoft-2025/P1/Documentation/Report/report-p1.md`
- **Additional Tests:** Modify `test-elasticsearch-quick.bat` as needed
- **Performance Benchmarking:** Optional if time permits
# Elasticsearch - Testing Guide

## Overview

This guide provides step-by-step instructions to test Elasticsearch functionality in the Library Management System.

---

## Prerequisites Setup

### Step 1: Start Elasticsearch Container

```cmd
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### Step 2: Wait for Elasticsearch to be Ready

**Wait ~30-60 seconds**, then verify:

```cmd
curl.exe http://localhost:9200
```

**Expected Response:**
```json
{
  "name" : "...",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "...",
  "version" : {
    "number" : "8.11.0",
    ...
  },
  "tagline" : "You Know, for Search"
}
```

### Step 3: Start Application with Elasticsearch Profile

**Before starting:** If the application is already running with another profile, stop it first (see "How to Stop the Application" below).

```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

**Watch startup logs for:**
```
INFO ... : The following profiles are active: "elasticsearch", "bootstrap"
INFO ... : Bootstrapping Spring Data Elasticsearch repositories
INFO ... : Creating Elasticsearch indices...
INFO ... : Bootstrap data creation started
```

**Note:** Keep this terminal window open - you'll need it to stop the app later.

---

## How to Stop the Application

**Option 1: Graceful shutdown (RECOMMENDED)**
- Go to the terminal where Maven is running
- Press `Ctrl+C`
- Wait for the application to shut down gracefully

**Option 2: Force stop (if Ctrl+C doesn't work)**

*Windows (CMD):*
```cmd
REM Find the process using port 8080
netstat -ano | findstr :8080

REM Kill the process (replace <PID> with actual number from output)
taskkill /F /PID <PID>

REM Or kill all Java processes (WARNING: stops ALL Java apps)
taskkill /F /IM java.exe
```

*Linux / Mac:*
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9

# Or
pkill -f spring-boot
```

---

## Switching Between Profiles

**To switch from SQL+Redis to Elasticsearch:**

1. Stop the current application (Ctrl+C or taskkill)
2. Verify it stopped:
   ```cmd
   netstat -ano | findstr :8080
   REM Should return nothing
   ```
3. Start with new profile:
   ```cmd
   mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
   ```

**To switch from Elasticsearch back to SQL+Redis:**

1. Stop the application
2. Start with SQL+Redis profile:
   ```cmd
   mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
   ```

---

## Verify Application is Running

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

---

## Test Credentials

### Readers
- **Email:** `manuel@gmail.com` | **Password:** `Manuelino123!`
- **Email:** `joao@gmail.com` | **Password:** `Joaozinho!123`

### Librarians
- **Email:** `maria@gmail.com` | **Password:** `Mariaroberta!123`
- **Email:** `admin@gmail.com` | **Password:** `AdminPwd1`

---

## Test 1: Verify Elasticsearch Indices

### Check All Indices

```cmd
curl.exe http://localhost:9200/_cat/indices?v
```

**Expected Output:**
```
health status index   uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   books   abc123...              1   1         10            0     50.1kb         50.1kb
yellow open   authors xyz789...              1   1          5            0     30.2kb         30.2kb
```

### Check Specific Index

```cmd
curl.exe http://localhost:9200/books/_search?size=1&pretty
```

**Expected:**
```json
{
  "took" : 5,
  "hits" : {
    "total" : { "value" : 10 },
    "hits" : [ ... book data ... ]
  }
}
```

---

## Test 2: List All Books

### Request

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

**Expected Response:**
```json
[
  {
    "isbn": "9789723716160",
    "title": "Sistemas Informáticos: Uma Perspetiva Empresarial",
    "genre": "Engenharia Informática",
    "authors": [...]
  },
  ...
]
```

---

## Test 3: Search Books by Title

### Search for "Spring"

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Spring"
```

**Expected:**
- Returns books with "Spring" in the title
- Elasticsearch full-text search applies

### Search for "Java"

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Java"
```

### Search for "Sistema"

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Sistema"
```

---

## Test 4: Get Book by ISBN

### Request

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/9789723716160
```

**Expected:**
```json
{
  "isbn": "9789723716160",
  "title": "Sistemas Informáticos: Uma Perspetiva Empresarial",
  "genre": "Engenharia Informática",
  "description": "...",
  "authors": [...]
}
```

---

## Test 5: Top 5 Books (Most Lent)

### Request

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

**Expected:**
```json
[
  {
    "isbn": "...",
    "title": "...",
    "lendingCount": 15
  },
  {
    "isbn": "...",
    "title": "...",
    "lendingCount": 12
  },
  ...
]
```

**Note:** If this returns empty `[]`, it may indicate:
- Bootstrap didn't create lending data
- Top5 query implementation needs adjustment
- **Strategic Decision:** Skip this test if problematic, focus on other features

---

## Test 6: Top 5 Authors

### Request

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

**Expected:**
```json
[
  {
    "authorNumber": "1/2025",
    "name": "...",
    "lendingCount": 25
  },
  ...
]
```

---

## Test 7: Search Authors by Name

### Request

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/authors/search?name=Silva"
```

**Expected:**
- Returns authors with "Silva" in their name

---

## Test 8: Create New Book (Librarian Only)

### Request

```cmd
curl.exe -X POST -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"isbn\":\"9781234567890\",\"title\":\"Test Book for Elasticsearch\",\"genre\":\"Technology\",\"description\":\"Testing Elasticsearch indexing\",\"authorIds\":[1]}" ^
  http://localhost:8080/api/books
```

**Expected:**
- ✅ Book created successfully (HTTP 201)
- 📝 Automatically indexed in Elasticsearch

### Verify Book in Elasticsearch

```cmd
curl.exe http://localhost:9200/books/_search?q=Test+Book&pretty
```

**Expected:**
- Shows the newly created book

### Search via API

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Test"
```

**Expected:**
- Returns the new "Test Book for Elasticsearch"

---

## Test 9: Update Book

### Request

```cmd
curl.exe -X PATCH -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"title\":\"Updated Test Book\"}" ^
  http://localhost:8080/api/books/9781234567890
```

**Expected:**
- ✅ Book updated (HTTP 200)
- 📝 Elasticsearch index automatically updated

### Verify Update

```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/9781234567890
```

**Expected:**
- Title shows "Updated Test Book"

---

## Test 10: Elasticsearch Direct Queries

### Count All Books

```cmd
curl.exe http://localhost:9200/books/_count
```

**Expected:**
```json
{
  "count": 11,
  "_shards": {...}
}
```

### Search with Elasticsearch Query DSL

```cmd
curl.exe -X POST http://localhost:9200/books/_search?pretty ^
  -H "Content-Type: application/json" ^
  -d "{\"query\":{\"match\":{\"title\":\"Sistema\"}}}"
```

**Expected:**
- Returns books matching "Sistema" in title

### Get All Books (Raw)

```cmd
curl.exe http://localhost:9200/books/_search?size=100&pretty
```

---

## Quick Test Script

Save this as `test-elasticsearch-quick.bat`:

```bat
@echo off
echo ========================================
echo QUICK ELASTICSEARCH TEST
echo ========================================
echo.

echo [1] Verify Elasticsearch is running
curl.exe http://localhost:9200
echo.
echo.

echo [2] Check indices
curl.exe http://localhost:9200/_cat/indices?v
echo.
echo.

echo [3] List all books (via API)
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
echo.
echo.

echo [4] Search books by title
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Spring"
echo.
echo.

echo [5] Top 5 authors
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
echo.
echo.

echo [6] Get book by ISBN
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/9789723716160
echo.
echo.

echo ========================================
echo DONE
echo ========================================
pause
```

---

## Troubleshooting

### Issue: Elasticsearch not responding

**Check if running:**
```cmd
docker ps | findstr elasticsearch
```

**Check logs:**
```cmd
docker logs elasticsearch
```

**Restart if needed:**
```cmd
docker restart elasticsearch
```

**Wait 30-60 seconds and test:**
```cmd
curl.exe http://localhost:9200
```

### Issue: No indices created

**Check application startup logs:**
- Should see "Creating Elasticsearch indices..."
- Should see "Bootstrap data creation started"

**Manually check indices:**
```cmd
curl.exe http://localhost:9200/_cat/indices?v
```

**If empty, restart application:**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### Issue: Authentication failures

**Use correct credentials:**
- **Readers:** `manuel@gmail.com:Manuelino123!`
- **Librarians:** `maria@gmail.com:Mariaroberta!123`

**Check user creation in logs:**
```
INFO ... : Created user: manuel@gmail.com with role: READER
INFO ... : Created user: maria@gmail.com with role: LIBRARIAN
```

### Issue: Search returns no results

**Possible causes:**
1. Bootstrap data not indexed
2. Search query doesn't match any books

**Verify books exist:**
```cmd
curl.exe http://localhost:9200/books/_count
```

**If count is 0:**
- Restart application with `bootstrap` profile

### Issue: Books/top5 returns empty

**Strategic decision:** 
- ⏭️ **Skip this test** if problematic
- ✅ Focus on other working features:
  - Book listing
  - Book search
  - Author listing
  - Author search
  - CRUD operations

**Reason:**
- Top5 requires complex aggregations + lending data
- May need additional time to debug
- Other features demonstrate Elasticsearch capabilities

---

## Comparison: SQL vs Elasticsearch

### Test Both Profiles

**SQL + Redis:**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**Elasticsearch:**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### Performance Comparison

| Operation | SQL + Redis | Elasticsearch |
|-----------|-------------|---------------|
| **List all books** | Fast (cached) | Fast (native) |
| **Search by title** | Medium (LIKE query) | **Very Fast** (full-text) |
| **Get by ISBN** | Fast (indexed) | Fast (indexed) |
| **Top 5 aggregation** | Medium | Fast (aggregations) |

### Feature Comparison

| Feature | SQL + Redis | Elasticsearch |
|---------|-------------|---------------|
| **Full-text search** | Limited | ✅ **Excellent** |
| **Caching** | ✅ Redis | Native |
| **Complex queries** | ✅ SQL power | Good |
| **Scalability** | Good | ✅ **Excellent** |
| **ACID transactions** | ✅ Full support | Limited |

---

## Success Criteria

✅ **Elasticsearch is working** if:
- Indices are created automatically
- Books can be listed via API
- Search returns relevant results
- CRUD operations work correctly
- Data persists across requests

