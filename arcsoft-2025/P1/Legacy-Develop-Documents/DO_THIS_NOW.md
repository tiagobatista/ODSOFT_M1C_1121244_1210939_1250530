# ⚡ DO THIS NOW - Elasticsearch Testing

**Created:** 2025-10-28  
**Time needed:** 5 minutes  
**Status:** READY TO GO! 🚀

---

## 🎯 YOUR MISSION

Test if the Elasticsearch bug fix worked!

---

## 📋 COPY-PASTE THESE COMMANDS

### Step 1: Clean Old Data (30 seconds)

Open terminal and paste:

```cmd
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f
```

Press `y` when asked to confirm.

---

### Step 2: Start Fresh Elasticsearch (15 seconds)

```cmd
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

Wait 15 seconds:

```cmd
timeout /t 15
```

---

### Step 3: Check Profile (10 seconds)

Open: `src\main\resources\application.properties`

Make sure it says:
```properties
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

If it doesn't, change it and save.

---

### Step 4: Restart Application (90 seconds)

```cmd
taskkill /F /IM java.exe
mvn spring-boot:run
```

**WAIT** for this message:
```
✅ Elasticsearch bootstrapping completed!
```

(Takes 60-90 seconds)

---

### Step 5: Run Tests (30 seconds)

```cmd
test-elasticsearch-corrected.bat
```

---

## ✅ WHAT YOU SHOULD SEE

**All these should show HTTP 200:**
- Admin - GET /api/books/top5
- Maria - POST /api/books/search
- Maria - GET /api/authors?name=Pina
- Manuel - GET /api/authors/top5

**These should show HTTP 403 (correct rejection):**
- Manuel - GET /api/books/top5

**These should show HTTP 401 (authentication failed):**
- Wrong password test
- Fake user test

---

## 🎉 SUCCESS LOOKS LIKE

```
[Test 1.1] Admin - GET /api/books/top5
HTTP Status: 200
Expected: 200

[Test 2.1] Maria (LIBRARIAN) - POST /api/books/search
HTTP Status: 200
Expected: 200

[Test 3.1] Maria - GET /api/authors?name=Pina
HTTP Status: 200
Expected: 200

[Test 4.2] Manuel (READER) - GET /api/authors/top5
HTTP Status: 200
Expected: 200

[Test 5.1] Manuel (READER) - GET /api/books/top5 (SHOULD FAIL - LIBRARIAN only)
HTTP Status: 403
Expected: 403 Forbidden
```

---

## ❌ IF SOMETHING FAILS

### Admin still gets 401?

```cmd
curl http://localhost:9200/users/_search?q=username:admin@gmail.com
```

Look for `"role": "LIBRARIAN"` in response.

### Maria gets 403?

```cmd
curl http://localhost:9200/users/_search?q=username:maria@gmail.com
```

Look for `"role": "LIBRARIAN"` in response (should be LIBRARIAN now!).

### Books search returns error?

Make sure you're using:
```cmd
curl.exe -X POST http://localhost:8080/api/books/search -u maria@gmail.com:Mariaroberta!123 -H "Content-Type: application/json" -d "{\"page\":{\"number\":1,\"limit\":10},\"query\":{}}"
```

**NOT:**
```cmd
curl.exe http://localhost:8080/api/books
```

---

## 📞 QUICK HELP

**Read if stuck:**
- `ELASTICSEARCH_QUICK_START.md` - Testing guide
- `SESSION_SUMMARY.md` - What was fixed
- `ELASTICSEARCH_ISSUES_RESOLVED.md` - Detailed debugging

**Check Elasticsearch is running:**
```cmd
docker ps
```

Should show `elasticsearch` container.

**Check data was created:**
```cmd
curl http://localhost:9200/users/_count
curl http://localhost:9200/books/_count
curl http://localhost:9200/authors/_count
```

---

## 🚀 AFTER TESTING

**If all tests pass:**
1. ✅ Mark Elasticsearch as COMPLETE
2. 🎉 Celebrate! The bug is fixed!
3. 📝 Update project documentation
4. 🚀 Move to next task (MongoDB or final testing)

**If tests fail:**
1. Read `ELASTICSEARCH_ISSUES_RESOLVED.md`
2. Check application logs
3. Verify Elasticsearch data with curl commands above
4. Double-check you cleaned volumes in Step 1

---

## ⏱️ TOTAL TIME: ~5 MINUTES

1. Clean data: 30 sec
2. Start Elasticsearch: 15 sec
3. Check profile: 10 sec
4. Restart app: 90 sec
5. Run tests: 30 sec

**Ready? GO!** 🏁

---

**All commands are ready to copy-paste. Just follow the steps!** ✅

