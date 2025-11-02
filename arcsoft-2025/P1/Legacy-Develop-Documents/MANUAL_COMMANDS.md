# ⚡ MANUAL COMMANDS - Run These One by One

**Date:** 2025-10-28  
**Purpose:** Manual testing after BCrypt password fix

---

## 📋 COPY-PASTE THESE COMMANDS

### Command 1: Stop Java
```cmd
taskkill /F /IM java.exe
```

### Command 2: Stop Elasticsearch
```cmd
docker stop elasticsearch
```

### Command 3: Remove Container
```cmd
docker rm elasticsearch
```

### Command 4: Clean Volumes (CRITICAL!)
```cmd
docker volume prune -f
```
Press `y` when asked.

### Command 5: Start Fresh Elasticsearch
```cmd
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### Command 6: Wait 15 Seconds
```cmd
timeout /t 15
```

### Command 7: Verify Elasticsearch Running
```cmd
docker ps
```
Should show `elasticsearch` container.

### Command 8: Start Application
```cmd
mvn spring-boot:run
```

**WAIT FOR:**
```
✅ Elasticsearch bootstrapping completed!
```

**CHECK FOR:**
- Should NOT see: `"Encoded password does not look like BCrypt"`
- Should see: `"Created 4 users"`

### Command 9: In NEW Terminal - Run Tests
```cmd
test-elasticsearch-corrected.bat
```

---

## ✅ EXPECTED RESULTS

All tests should return **HTTP 200** (not 401):
- Admin - GET /api/books/top5 → **200** ✅
- Maria - POST /api/books/search → **200** ✅
- Maria - GET /api/authors?name=Pina → **200** ✅
- Manuel - GET /api/authors/top5 → **200** ✅

Manuel on LIBRARIAN endpoint should return **HTTP 403** (correct rejection).

---

## 🔍 VERIFY PASSWORD ENCODING

```cmd
curl http://localhost:9200/users/_search?q=username:admin@gmail.com
```

Look for password starting with `$2a$` or `$2b$` (BCrypt hash).

---

## 🐛 IF STILL 401

Check application logs for:
```
WARN ... BCryptPasswordEncoder : Encoded password does not look like BCrypt
```

If you see this, you didn't clean volumes! Run:
```cmd
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f
```

Then restart from Command 5.

---

**Total time: ~5 minutes**

