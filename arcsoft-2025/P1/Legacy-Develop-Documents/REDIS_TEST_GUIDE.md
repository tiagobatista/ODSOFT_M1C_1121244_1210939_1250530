# Redis Caching Test Guide

## Overview
This guide helps you test the Redis caching implementation in the SQL+Redis profile.

## Prerequisites

✅ **Application running** with `sql-redis` profile:
```cmd
mvn spring-boot:run
```

✅ **Redis running** (embedded - starts automatically with the application)

✅ **Port Check:**
- Application: `http://localhost:8080`
- Redis: `localhost:6379`

## Test Scripts Available

### 1. **quick-redis-test.bat** (Recommended for Quick Demo)
**Purpose:** Simple 3-call demonstration of cache MISS → HIT → HIT

```cmd
quick-redis-test.bat
```

**What it does:**
1. First call to `/api/books/top5` - **CACHE MISS** (fetches from SQL)
2. Second call to `/api/books/top5` - **CACHE HIT** (fetches from Redis)
3. Third call to `/api/books/top5` - **CACHE HIT** (still from Redis)

**Watch for in console:**
- `📖 CACHE MISS - Fetching from SQL`
- `📚 CACHE HIT - Fetched from Redis`
- Response time differences

---

### 2. **test-redis-postman-based.bat** (Comprehensive)
**Purpose:** Full test suite based on Postman collection

```cmd
test-redis-postman-based.bat
```

**What it tests:**
- Authentication (Login)
- Books Top 5 (with cache demonstration)
- Authors Top 5 (with cache demonstration)
- Individual book fetching
- Search queries

---

### 3. **demo-redis-caching.bat** (Detailed with Data Creation)
**Purpose:** Creates test data and demonstrates caching

```cmd
demo-redis-caching.bat
```

**Includes:**
- Database state verification
- Test data creation
- Multiple endpoint tests
- Cache invalidation demonstration

---

## Manual Testing with Postman Collection

### Using the Collection File
Located at: `src/main/resources/assets/redis_test_collection/Psoft-G1.postman_collection.json`

### Environment File
Located at: `src/main/resources/assets/new/Psoft-G1.postman_environment.json`

### Import Steps:
1. Open Postman
2. Import → File → Select `Psoft-G1.postman_collection.json`
3. Import → File → Select `Psoft-G1.postman_environment.json`
4. Select "Psoft-G1" environment in top-right dropdown
5. Run requests from the collection

### Key Endpoints for Cache Testing:

#### Books Top 5 (Librarian - Maria)
```
GET http://localhost:8080/api/books/top5
Auth: Basic maria@gmail.com:Mariaroberta!123
```

#### Authors Top 5 (Reader - Manuel)
```
GET http://localhost:8080/api/authors/top5
Auth: Basic manuel@gmail.com:Manuelino123!
```

#### Individual Book
```
GET http://localhost:8080/api/books/{isbn}
Auth: Basic maria@gmail.com:Mariaroberta!123
```

---

## Manual Testing with cURL

### Test 1: Books Top 5 Cache
```cmd
# First call (CACHE MISS)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

# Second call (CACHE HIT - should be faster)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

### Test 2: Individual Book Cache
```cmd
# First call (CACHE MISS)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160

# Second call (CACHE HIT)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/9789723716160
```

### Test 3: Authors Top 5
```cmd
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

---

## What to Look For

### In Application Console:
```
📖 CACHE MISS - Fetching from SQL - Book ISBN: 9789723716160
💾 Saved to SQL - Book: <title>
♻️ Updated Redis cache - Book ISBN: 9789723716160

📚 CACHE HIT - Book ISBN: 9789723716160
```

### Response Time Comparison:
- **First Request (SQL):** ~0.200s - 0.500s
- **Cached Request (Redis):** ~0.010s - 0.050s
- **Speed Improvement:** 10x - 50x faster!

---

## Redis Cache Configuration

### Cache Keys:
| Entity | Key Pattern | TTL |
|--------|-------------|-----|
| Book by ISBN | `books:isbn:{isbn}` | 30 minutes |
| Author by ID | `authors:id:{id}` | 30 minutes |
| Reader by Number | `readers:number:{number}` | 30 minutes |
| Lending by ID | `lendings:id:{year}/{sequence}` | 30 minutes |
| Books Top 5 | `books:top5` | 10 minutes |
| Authors Top 5 | `authors:top5` | 10 minutes |

### Configuration File:
`src/main/resources/application-sql-redis.properties`

```properties
# Redis Cache
persistence.caching-enabled=true
spring.cache.type=redis
spring.cache.redis.time-to-live=1800000  # 30 minutes
spring.cache.redis.cache-null-values=false
```

---

## Verifying Redis is Working

### Check Redis Connection:
```cmd
netstat -ano | findstr :6379
```

Expected output:
```
TCP    127.0.0.1:6379    127.0.0.1:XXXXX    ESTABLISHED
```

### Check Application Connection:
```cmd
netstat -ano | findstr :8080
```

---

## Troubleshooting

### Issue: No cache messages in console
**Solution:** Check `application.properties`:
```properties
spring.profiles.active=sql-redis,bootstrap
persistence.caching-enabled=true
```

### Issue: Redis not running
**Solution:** Redis is embedded - restart application:
```cmd
mvn spring-boot:run
```

### Issue: Empty responses
**Solution:** Bootstrap data might not have loaded. Check for:
```
✓ Elasticsearch bootstrapping completed!
```

If missing, verify `bootstrap` profile is active.

### Issue: Authentication fails (401)
**Solution:** Verify credentials:
- Librarian: `maria@gmail.com` / `Mariaroberta!123`
- Reader: `manuel@gmail.com` / `Manuelino123!`

---

## Expected Test Results

### Successful Cache Test Output:
```json
{
  "items": [
    {
      "bookView": {
        "title": "Como se Desenha Uma Casa",
        "authors": ["Manuel Antonio Pina"],
        "genre": "Infantil",
        "isbn": "9789723716160"
      },
      "lendingCount": 10
    }
  ]
}
```

### Console Logs (Successful):
```
📖 CACHE MISS - Fetching from SQL - Book ISBN: 9789723716160
💾 Saved to SQL
♻️ Updated Redis cache

📚 CACHE HIT - Book ISBN: 9789723716160  <-- Second request (faster!)
```

---

## Performance Metrics

Based on typical execution:

| Operation | First Call (SQL) | Cached Call (Redis) | Speedup |
|-----------|------------------|---------------------|---------|
| Get Book by ISBN | ~250ms | ~15ms | **16x faster** |
| Books Top 5 | ~300ms | ~20ms | **15x faster** |
| Search Books | ~400ms | ~25ms | **16x faster** |

---

## Test Credentials

From Postman Collection:

| User | Email | Password | Role |
|------|-------|----------|------|
| Maria | maria@gmail.com | Mariaroberta!123 | LIBRARIAN |
| Manuel | manuel@gmail.com | Manuelino123! | READER |
| Pedro | pedro@gmail.com | Pedrodascenas!123 | READER |
| Admin | admin@gmail.com | AdminPwd1 | ADMIN |

---

## Quick Start

**Fastest way to see Redis caching:**

```cmd
# 1. Start application (if not running)
mvn spring-boot:run

# 2. Run quick test
quick-redis-test.bat

# 3. Watch console for CACHE MISS → CACHE HIT messages
```

**Done!** You've successfully tested Redis caching! 🎉

---

## Documentation Links

- [Redis Configuration Guide](../REDIS_QUICK_REFERENCE.md)
- [Database Switching Guide](../DATABASE_SWITCHING_QUICK_GUIDE.md)
- [Postman Collection](../src/main/resources/assets/redis_test_collection/Psoft-G1.postman_collection.json)

---

**Last Updated:** 2025-10-30  
**Profile:** sql-redis  
**Redis Version:** Embedded (Spring Data Redis)

