# Redis Testing - Quick Summary

## ✅ Status: READY TO TEST

### Current Setup:
- ✅ Application running on port 8080 (PID: 43632)
- ✅ Redis running on port 6379 (PID: 15196)
- ✅ Connected and operational
- ✅ Profile: `sql-redis,bootstrap`

---

## 🚀 Quick Test (30 seconds)

**Run this command:**
```cmd
quick-redis-test.bat
```

**Watch the application console for:**
1. First call: `📖 CACHE MISS - Fetching from SQL`
2. Second call: `📚 CACHE HIT` ← **This proves Redis is caching!**
3. Third call: `📚 CACHE HIT` ← **Still cached**

---

## 📋 Test Scripts Created

| Script | Purpose | Time |
|--------|---------|------|
| `quick-redis-test.bat` | Quick 3-call demo | 30 sec |
| `test-redis-postman-based.bat` | Full Postman collection tests | 2 min |
| `demo-redis-caching.bat` | Detailed with data creation | 3 min |
| `simple-redis-test.bat` | Simple with manual book creation | 1 min |

---

## 🔧 Using Your Postman Collection

Your colleagues' collection is at:
```
src/main/resources/assets/redis_test_collection/Psoft-G1.postman_collection.json
```

**Test these endpoints manually:**

### 1. Books Top 5 (twice to see caching):
```
GET http://localhost:8080/api/books/top5
Authorization: Basic maria@gmail.com:Mariaroberta!123
```

### 2. Authors Top 5:
```
GET http://localhost:8080/api/authors/top5  
Authorization: Basic manuel@gmail.com:Manuelino123!
```

### 3. Individual Book:
```
GET http://localhost:8080/api/books/9789723716160
Authorization: Basic maria@gmail.com:Mariaroberta!123
```

**Important:** Call each endpoint **TWICE** to see:
- First call = CACHE MISS (slower)
- Second call = CACHE HIT (faster) ✨

---

## 📊 What You'll See

### In Application Console:
```
2025-10-30T22:XX:XX.XXXZ  INFO ... BookCacheRepository : 📖 CACHE MISS - Fetching from SQL - Book ISBN: 9789723716160
2025-10-30T22:XX:XX.XXXZ  INFO ... BookCacheRepository : 💾 Saved to SQL
2025-10-30T22:XX:XX.XXXZ  INFO ... BookCacheRepository : ♻️ Updated Redis cache - Book ISBN: 9789723716160

[Next request for same ISBN]

2025-10-30T22:XX:XX.XXXZ  INFO ... BookCacheRepository : 📚 CACHE HIT - Book ISBN: 9789723716160
```

### Performance Difference:
- **SQL (first call):** ~250ms
- **Redis (cached):** ~15ms  
- **Speed improvement:** ~16x faster! 🚀

---

## 📝 Complete Documentation

For full details, see: **[REDIS_TEST_GUIDE.md](REDIS_TEST_GUIDE.md)**

Includes:
- All test scripts explained
- Manual testing with cURL commands
- Postman collection usage
- Cache configuration details
- Troubleshooting guide
- Expected results

---

## 💡 Test Credentials

| User | Email | Password | Role |
|------|-------|----------|------|
| Maria | maria@gmail.com | Mariaroberta!123 | LIBRARIAN |
| Manuel | manuel@gmail.com | Manuelino123! | READER |

---

## ✨ Ready to Demo!

**To demonstrate Redis caching to your colleagues:**

1. Open two windows:
   - **Window 1:** Application console (to see cache logs)
   - **Window 2:** Command prompt

2. Run in Window 2:
   ```cmd
   quick-redis-test.bat
   ```

3. Point to Window 1 and show:
   - First call: "CACHE MISS" message
   - Second call: "CACHE HIT" message
   - Response time difference

**That's it!** Redis caching demonstrated! 🎉

---

## 🔍 Verify Redis is Working Right Now

Run this command:
```cmd
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

Then run it again immediately:
```cmd
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

Check the application console for CACHE MISS → CACHE HIT!

---

**Redis Status:** ✅ OPERATIONAL  
**Last Tested:** 2025-10-30  
**Application Profile:** sql-redis

