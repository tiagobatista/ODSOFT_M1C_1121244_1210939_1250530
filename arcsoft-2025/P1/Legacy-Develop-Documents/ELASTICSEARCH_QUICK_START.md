# 🚀 QUICK START - ELASTICSEARCH TESTING

**Date:** 2025-10-28  
**Purpose:** Quick reference for testing Elasticsearch implementation after bug fixes

---

## ⚡ IMMEDIATE ACTION REQUIRED

### 1. Clean Elasticsearch (Delete old data with wrong user roles)

```cmd
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f
```

### 2. Start Fresh Elasticsearch

```cmd
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 ^
  -e "discovery.type=single-node" ^
  -e "xpack.security.enabled=false" ^
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" ^
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

timeout /t 15
```

### 3. Restart Application

```cmd
taskkill /F /IM java.exe
mvn spring-boot:run
```

**Wait 60-90 seconds for bootstrap!** Watch for: `"✅ Elasticsearch bootstrapping completed!"`

---

## 🧪 QUICK TESTS

### Test Admin (Fixed!)

```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
```
**Expected:** HTTP 200 with book data

### Test Books Search (Correct endpoint!)

```cmd
curl.exe -X POST http://localhost:8080/api/books/search ^
  -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"page\":{\"number\":1,\"limit\":10},\"query\":{}}"
```
**Expected:** HTTP 200 with book list

### Test Authors Search (With required parameter!)

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/authors?name=Pina"
```
**Expected:** HTTP 200 with author list

---

## 👥 USER CREDENTIALS (CORRECTED ROLES)

| Email | Password | Role | Status |
|-------|----------|------|--------|
| admin@gmail.com | AdminPwd1 | **LIBRARIAN** | ✅ Fixed |
| maria@gmail.com | Mariaroberta!123 | **LIBRARIAN** | ✅ Fixed |
| manuel@gmail.com | Manuelino123! | **READER** | ✅ Fixed |
| antonio@gmail.com | Antonio123! | **READER** | ✅ OK |

---

## 📋 CORRECT ENDPOINT USAGE

### ❌ WRONG (These will fail!)

```cmd
# NO! Endpoint doesn't exist
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books

# NO! Missing required parameter
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/authors
```

### ✅ CORRECT

```cmd
# YES! Use POST /api/books/search
curl.exe -X POST http://localhost:8080/api/books/search ^
  -u maria@gmail.com:Mariaroberta!123 ^
  -H "Content-Type: application/json" ^
  -d "{\"page\":{\"number\":1,\"limit\":10},\"query\":{}}"

# YES! Add ?name= parameter
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/authors?name=Pina"
```

---

## 🔍 VERIFY FIX WORKED

### Check Elasticsearch has correct users

```cmd
curl http://localhost:9200/users/_search?q=username:maria@gmail.com
```

Look for `"role": "LIBRARIAN"` in the response

```cmd
curl http://localhost:9200/users/_search?q=username:manuel@gmail.com
```

Look for `"role": "READER"` in the response

---

## 📊 RUN AUTOMATED TESTS

```cmd
test-elasticsearch-corrected.bat
```

**All tests should now pass!**

---

## 🐛 IF ISSUES PERSIST

1. Check application logs for bootstrap messages
2. Verify Elasticsearch is running: `docker ps`
3. Check data in Elasticsearch: `curl http://localhost:9200/users/_search?pretty`
4. Try with verbose curl: `curl.exe -v -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5`

---

## 📁 KEY FILES CHANGED

- `src/main/java/pt/psoft/g1/psoftg1/bootstrapping/ElasticsearchBootstrapper.java`
  - Fixed user role assignments (Manuel→READER, Maria→LIBRARIAN)

---

## 🎯 EXPECTED RESULTS

✅ Admin authentication works (HTTP 200)  
✅ Maria can access LIBRARIAN endpoints (HTTP 200)  
✅ Manuel can access READER endpoints (HTTP 200)  
✅ Manuel gets 403 on LIBRARIAN endpoints (correct rejection)  
✅ POST `/api/books/search` works (HTTP 200)  
✅ GET `/api/authors?name=...` works (HTTP 200)  

---

**All issues resolved! Ready to continue Elasticsearch development! 🚀**

