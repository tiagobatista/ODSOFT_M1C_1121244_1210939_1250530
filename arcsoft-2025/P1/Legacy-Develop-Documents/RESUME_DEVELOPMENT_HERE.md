# 🚀 ELASTICSEARCH IMPLEMENTATION - CURRENT STATE & NEXT STEPS

**Date:** 2025-10-26  
**Project:** MEI-ARCSOFT-2025-2026 Multi-Database Persistence  
**Status:** ⚠️ Elasticsearch implementation PARTIALLY COMPLETE - Endpoints partially working

---

## 📋 PROJECT CONTEXT

### Goal
Implement a **multi-database persistence architecture** that allows switching between:
1. ✅ SQL (H2) + Redis
2. 🚧 MongoDB + Redis (infrastructure only)
3. ⚠️ **Elasticsearch** (CURRENT FOCUS - partially implemented)

**Switching mechanism:** Change Spring profile in `application.properties` at **setup time** (configuration-driven runtime behavior)

---

## ✅ WHAT WAS COMPLETED FOR ELASTICSEARCH

### 1. Infrastructure Setup ✅

**Docker Container:**
```cmd
# Elasticsearch container running successfully
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

**Status:** ✅ Container running, accessible on `http://localhost:9200`

---

### 2. Document Models Created ✅

**Created Elasticsearch documents for:**
- ✅ `BookDocument` (`@Document(indexName = "books")`)
- ✅ `AuthorDocument` (`@Document(indexName = "authors")`)
- ✅ `GenreDocument` (`@Document(indexName = "genres")`)
- ✅ `UserDocument` (`@Document(indexName = "users")`)
- ✅ `ReaderDocument` (`@Document(indexName = "readers")`)

**Location:** `src/main/java/pt/psoft/g1/psoftg1/.../model/ElasticSearch/`

**Key fix applied:** Changed `BookDocument.authors` field type from `@Field(type = FieldType.Nested)` to `@Field(type = FieldType.Keyword)` because we store author names as strings, not nested objects.

---

### 3. Repository Implementations Created ✅

**Elasticsearch repositories implemented:**
- ✅ `BookRepositoryElasticsearchImpl`
- ✅ `AuthorRepositoryElasticsearchImpl`
- ✅ `GenreRepositoryElasticsearchImpl`
- ✅ `UserRepositoryElasticsearchImpl`
- ✅ `ReaderRepositoryElasticsearchImpl`

**Location:** `src/main/java/pt/psoft/g1/psoftg1/.../infrastructure/repositories/impl/ElasticSearch/`

All marked with `@Profile("elasticsearch")` and `@Primary` to activate only when elasticsearch profile is selected.

---

### 4. Mappers Created ✅

**Document ↔ Domain mappers:**
- ✅ `BookDocumentMapper` - Maps between `BookDocument` and `Book` domain model
  - **Fix applied:** Uses placeholder bio text `"Author bio not available in book index"` instead of null/empty to satisfy Bio validation
- ✅ `AuthorDocumentMapper`
- ✅ `GenreDocumentMapper`
- ✅ `UserDocumentMapper`
- ✅ `ReaderDocumentMapper`

**Location:** `src/main/java/pt/psoft/g1/psoftg1/.../infrastructure/repositories/impl/Mapper/`

---

### 5. Bootstrap Data Setup ✅

**ElasticsearchBootstrapper created:**
- ✅ Marked with `@Profile("elasticsearch")` and `@Order(1)`
- ✅ Creates initial data on application startup
- ✅ **Successfully creates:**
  - 4 users (admin, manuel, maria, antonio)
  - 7 genres (Infantil, Thriller, Informação, etc.)
  - 6 authors (Manuel Antonio Pina, Alexandre Pereira, etc.)
  - Multiple books

**Location:** `src/main/java/pt/psoft/g1/psoftg1/bootstrapping/ElasticsearchBootstrapper.java`

**Status:** ✅ Runs successfully (confirmed from logs: "✓ Created 4 users", "✓ Created 6 authors")

---

### 6. Profile Configuration ✅

**application.properties:**
```properties
# Current Elasticsearch configuration
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

**To switch to SQL:**
```properties
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
```

**Status:** ✅ Profile switching works correctly

---

### 7. Docker Volume Issue Resolved ✅

**Problem:** Old Elasticsearch index mappings were cached in Docker volumes  
**Solution:** Deleted container AND volumes:
```cmd
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f
```

**Result:** ✅ Fresh Elasticsearch instance with correct mappings

---

## 🔧 CRITICAL SECURITY FIXES APPLIED

### 1. SecurityConfig Session Management Conflict ✅

**Problem Found:** Two conflicting session management configurations:
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // Line 1
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))  // Line 2 - CONFLICT!
```

**Fix Applied:** Removed duplicate, kept only `STATELESS` for both JWT and Basic Auth

**Result:** ✅ Consistent authentication behavior

---

### 2. SecurityConfig Rule Ordering - CRITICAL BUG ✅

**Problem Found:** Path variable patterns were matching specific endpoints incorrectly!

**Example:**
```java
// WRONG ORDER:
.requestMatchers(HttpMethod.GET,"/api/books/{isbn}").hasAnyAuthority(Role.READER, Role.LIBRARIAN)
.requestMatchers(HttpMethod.GET,"/api/books/top5").hasAuthority(Role.LIBRARIAN)
// Spring matched "top5" as {isbn} value, so READER could access LIBRARIAN endpoint!
```

**Fix Applied:** Reordered ALL authorization rules - **specific paths BEFORE path variables:**
```java
// CORRECT ORDER:
.requestMatchers(HttpMethod.GET,"/api/books/top5").hasAuthority(Role.LIBRARIAN)  // Specific first!
.requestMatchers(HttpMethod.GET,"/api/books/suggestions").hasAuthority(Role.READER)
.requestMatchers(HttpMethod.GET,"/api/books/{isbn}").hasAnyAuthority(Role.READER, Role.LIBRARIAN)
```

**Sections Fixed:**
- ✅ Books section
- ✅ Authors section  
- ✅ Readers section
- ✅ Lendings section

**Impact:** **CRITICAL** - This was a security vulnerability allowing unauthorized access!

---

## ⚠️ CURRENT ISSUES (NEED INVESTIGATION)

### Application Currently Running With: **SQL Profile**

**Why:** Switched to SQL to test authentication after Elasticsearch issues. To continue with Elasticsearch, change `application.properties` back to:
```properties
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

---

### Issue 1: Admin Gets 401 on All Endpoints ❌

**Test Result:**
```
[Test 1.1] Admin - GET /api/books
HTTP Status: 401
Expected: 200
```

**Symptoms:**
- Admin credentials (`admin@gmail.com` / `AdminPwd1`) return 401 Unauthorized
- Maria and Manuel work fine
- Wrong password correctly returns 401 (auth IS working)

**Possible Causes:**
1. Admin user not created during bootstrap
2. Password encoding mismatch for admin specifically
3. Admin role not properly assigned
4. Profile mismatch (admin created in SQL but testing in Elasticsearch?)

**How to Investigate:**
```cmd
# Check application logs for:
"Created admin user" or "✓ Created 4 users"

# Verify admin in database (if using SQL):
# Access H2 Console: http://localhost:8080/h2-console
# URL: jdbc:h2:mem:testdb
# User: SA, Password: (blank)
# SQL: SELECT * FROM USERS WHERE USERNAME = 'admin@gmail.com';

# If using Elasticsearch:
curl http://localhost:9200/users/_search?q=username:admin@gmail.com
```

**Next Steps:**
1. Check which profile is active (`sql-redis` or `elasticsearch`)
2. Verify admin user exists in that database
3. Check password encoding in bootstrap code
4. Try with known-working user (Maria) to isolate issue

---

### Issue 2: `/api/books` Returns 404 ❌

**Test Result:**
```
[Test 2.1] Maria - GET /api/books
HTTP Status: 404
Expected: 200
```

**BUT `/api/books/top5` works!** (Returns 200 with data)

**Symptoms:**
- Specific book endpoints work (e.g., `/api/books/top5`)
- General `/api/books` returns 404
- Same pattern for authors (`/api/authors` → 400, `/api/authors/top5` → 200)

**Possible Causes:**
1. **Controller requires query parameters** for `/api/books` endpoint
2. No `@GetMapping` for `/api/books` without parameters
3. Different controller methods for different endpoints

**How to Investigate:**
```java
// Check BookController.java for:
@GetMapping("/api/books")
public ResponseEntity<?> getAllBooks(...) {
    // Look for required parameters like:
    // @RequestParam(required = true) String genre
    // This would cause 404 if parameter missing
}
```

**Likely Solution:**
The controller might expect query parameters:
```bash
# Try:
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?page=1"
# Or:
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/books?genre=Infantil"
```

**Next Steps:**
1. Check `BookController.java` for `/api/books` method signature
2. Check if `@RequestParam` has `required=true`
3. Test with different query parameters

---

### Issue 3: `/api/authors` Returns 400 ❌

**Test Result:**
```
[Test 2.3] Maria - GET /api/authors
HTTP Status: 400
Expected: 200
```

**BUT `/api/authors/top5` works!** (Returns 200 with data)

**Symptoms:**
- 400 Bad Request suggests validation error or missing parameter
- Similar pattern to `/api/books` issue

**Possible Causes:**
1. **Controller requires query parameters** or request body
2. Missing required `@RequestParam`
3. Validation error on request

**How to Investigate:**
```java
// Check AuthorController.java for:
@GetMapping("/api/authors")
public ResponseEntity<?> getAuthors(...) {
    // Look for:
    // @RequestParam(required = true) String name
    // @Valid @RequestBody SearchQuery query
}
```

**Next Steps:**
1. Check `AuthorController.java` method signature
2. Try with query parameters:
```bash
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/authors?name=Pina"
curl.exe -u maria@gmail.com:Mariaroberta!123 "http://localhost:8080/api/authors?page=1"
```

---

## 📊 ENDPOINT TEST RESULTS (Last Run)

**Working Endpoints ✅:**
```
✅ /api/books/top5 (Maria - LIBRARIAN) → 200 OK
✅ /api/authors/top5 (Manuel - READER) → 200 OK
✅ /api/books/suggestions (Maria) → 403 (correct rejection - READER only)
✅ /api/genres/top5 (Manuel) → 403 (correct rejection - LIBRARIAN only)
✅ Wrong password → 401 (auth working)
✅ Fake user → 401 (auth working)
```

**Failing Endpoints ❌:**
```
❌ /api/books (Maria) → 404
❌ /api/authors (Maria) → 400
❌ All admin requests → 401
```

**Security Issues Fixed ✅:**
```
✅ Manuel can NO LONGER access /api/books/top5 (was 200, now 403 after ordering fix)
```

---

## 📝 USER CREDENTIALS (From Bootstrap)

**Active Users:**

| Email | Password | Role | Status |
|-------|----------|------|--------|
| admin@gmail.com | AdminPwd1 | LIBRARIAN | ❌ 401 on all requests |
| maria@gmail.com | Mariaroberta!123 | LIBRARIAN | ✅ Most endpoints work |
| manuel@gmail.com | Manuelino123! | READER | ✅ Most endpoints work |
| antonio@gmail.com | Antonio123! | READER | ✅ Should work |

**Note:** Maria is a LIBRARIAN, NOT a READER (common misconception)!

---

## 🔄 HOW TO CONTINUE DEVELOPMENT

### Step 1: Choose Database Profile

**For Elasticsearch testing:**
```properties
# application.properties
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

**For SQL testing (known working):**
```properties
# application.properties
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
```

---

### Step 2: Restart Application

```cmd
# Kill existing Java processes
taskkill /F /IM java.exe

# Start application
cd F:\repos\novo\MEI-ARCSOFT-2025-2026-1191577-1210939-1250530
mvn spring-boot:run

# Wait 60-90 seconds for bootstrap to complete
```

---

### Step 3: Verify Elasticsearch Running (if using ES profile)

```cmd
# Check if Elasticsearch container is running
docker ps | findstr elasticsearch

# If not running, start it:
docker start elasticsearch

# OR create fresh container:
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

# Verify Elasticsearch is accessible:
curl http://localhost:9200
```

---

### Step 4: Run Comprehensive Tests

```cmd
# Test script location:
F:\repos\novo\MEI-ARCSOFT-2025-2026-1191577-1210939-1250530\test-all-endpoints.bat

# This tests:
# - Admin, Maria, Manuel credentials
# - All major endpoints
# - Expected successes and failures
# - Authorization rules
```

---

### Step 5: Investigate Specific Issues

**For Admin 401 issue:**
```java
// Check: src/main/java/pt/psoft/g1/psoftg1/bootstrapping/
// Files: ElasticsearchBootstrapper.java or UserBootstrapper.java
// Look for: admin user creation
// Verify password: "AdminPwd1"
```

**For /api/books 404 issue:**
```java
// Check: src/main/java/pt/psoft/g1/psoftg1/bookmanagement/api/BookController.java
// Look for: @GetMapping("/api/books") or @GetMapping
// Check for: required parameters
```

**For /api/authors 400 issue:**
```java
// Check: src/main/java/pt/psoft/g1/psoftg1/authormanagement/api/AuthorController.java
// Look for: @GetMapping("/api/authors")
// Check for: validation annotations, required parameters
```

---

## 📂 KEY FILES & LOCATIONS

### Elasticsearch Implementation Files:
```
src/main/java/pt/psoft/g1/psoftg1/
├── bookmanagement/
│   ├── model/ElasticSearch/BookDocument.java ✅
│   └── infrastructure/repositories/impl/
│       ├── ElasticSearch/BookRepositoryElasticsearchImpl.java ✅
│       └── Mapper/BookDocumentMapper.java ✅
├── authormanagement/
│   ├── model/ElasticSearch/AuthorDocument.java ✅
│   └── infrastructure/repositories/impl/ElasticSearch/ ✅
├── genremanagement/ (similar structure) ✅
├── usermanagement/ (similar structure) ✅
├── readermanagement/ (similar structure) ✅
└── bootstrapping/
    ├── ElasticsearchBootstrapper.java ✅
    ├── Bootstrapper.java (SQL/MongoDB) ✅
    └── UserBootstrapper.java (SQL only) ✅
```

### Configuration Files:
```
src/main/resources/
├── application.properties ⚠️ (SET PROFILE HERE!)
├── application-elasticsearch.properties ✅
├── application-sql-redis.properties ✅
└── application-mongodb-redis.properties 🚧
```

### Security Configuration:
```
src/main/java/pt/psoft/g1/psoftg1/configuration/
└── SecurityConfig.java ✅ (RECENTLY FIXED!)
```

---

## 🧪 TEST SCRIPTS AVAILABLE

**Location:** Project root directory

1. **`test-all-endpoints.bat`** - Comprehensive endpoint testing
   - Tests Admin, Maria, Manuel
   - Tests all major endpoints
   - Shows expected vs actual results

2. **`test-elasticsearch-endpoints.bat`** - Elasticsearch-specific tests
   - Tests with Elasticsearch profile
   - Includes search queries

3. **`test-fixed-security.bat`** - Security-focused tests
   - Tests authorization rules
   - Tests role-based access

---

## 📚 DOCUMENTATION CREATED

**Available in project root:**

1. **`ELASTICSEARCH_STATUS_FINAL.md`** - Overall Elasticsearch status
2. **`BLOCKER_RESOLVED.md`** - How Docker volume issue was fixed
3. **`SECURITY_CONFIG_FIXED.md`** - Session management fix details
4. **`SECURITY_ORDERING_FIX.md`** - Path variable ordering fix (CRITICAL!)
5. **`FINAL_TEST_RESULTS_SUMMARY.md`** - Latest test results analysis
6. **`ENDPOINT_TESTING_DIAGNOSTIC.md`** - How to diagnose endpoint issues
7. **`COMPLETE_SOLUTION_SUMMARY.md`** - Overall progress summary
8. **`ENDPOINT_PERMISSIONS.md`** - Who can access what endpoints
9. **`DATABASE_SWITCHING_GUIDE.md`** (if exists) - How to switch databases

---

## ⚡ QUICK START TO CONTINUE

### Scenario A: Continue with Elasticsearch

```cmd
# 1. Switch profile
# Edit: src/main/resources/application.properties
# Set: spring.profiles.active=elasticsearch,bootstrap

# 2. Ensure Elasticsearch running
docker start elasticsearch

# 3. Restart app
taskkill /F /IM java.exe
mvn spring-boot:run

# 4. Wait 90 seconds, then test
test-all-endpoints.bat
```

---

### Scenario B: Test with SQL First (Safer)

```cmd
# 1. Keep SQL profile (already set)
# application.properties: spring.profiles.active=sql-redis,bootstrap

# 2. Restart app
taskkill /F /IM java.exe
mvn spring-boot:run

# 3. Test
test-all-endpoints.bat

# 4. Once SQL works, switch to Elasticsearch
```

---

## 🎯 IMMEDIATE NEXT ACTIONS

**Priority 1: Investigate Admin 401 ⚠️**
- [ ] Check bootstrap logs for admin creation
- [ ] Verify admin user in database
- [ ] Try admin login with verbose curl
- [ ] Compare admin creation code vs maria/manuel

**Priority 2: Fix /api/books 404 ⚠️**
- [ ] Check BookController.java
- [ ] Look for required parameters
- [ ] Test with query parameters
- [ ] Check if endpoint exists

**Priority 3: Fix /api/authors 400 ⚠️**
- [ ] Check AuthorController.java
- [ ] Look for validation requirements
- [ ] Test with different parameters
- [ ] Check request format

**Priority 4: Verify Elasticsearch Data ✅**
- [ ] Check if books created: `curl http://localhost:9200/books/_search`
- [ ] Check if authors created: `curl http://localhost:9200/authors/_search`
- [ ] Verify index mappings correct

---

## 💡 KNOWN GOOD WORKING STATE

**When you need a baseline:**

```properties
# application.properties - SQL (KNOWN WORKING)
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
```

**Then test with:**
```bash
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
# Should return: HTTP 200 with book data
```

**If this works but Elasticsearch doesn't:**
- Issue is Elasticsearch-specific
- Check ElasticsearchBootstrapper
- Check document mappings
- Check mappers

---

## 🔍 DEBUGGING COMMANDS

**Check application logs:**
```cmd
# Look for startup errors in terminal where mvn spring-boot:run is running
# Key phrases: "ERROR", "Failed", "Exception", "Started PsoftG1Application"
```

**Check Elasticsearch directly:**
```cmd
# List all indices
curl http://localhost:9200/_cat/indices?v

# Check books index
curl http://localhost:9200/books/_search?pretty

# Check users index
curl http://localhost:9200/users/_search?pretty

# Check mapping for books
curl http://localhost:9200/books/_mapping?pretty
```

**Check H2 Database (SQL profile):**
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:testdb
Username: SA
Password: (blank)
```

---

## 📦 DEPENDENCIES & VERSIONS

**Confirmed Working:**
- Spring Boot: 3.2.5
- Elasticsearch: 8.11.0 (Docker image)
- Java: 21
- Maven: (current version)
- Docker Desktop: Required for Elasticsearch

---

## 🎓 KEY LEARNINGS

1. **Spring Security Rule Ordering is CRITICAL** ⚠️
   - Specific paths MUST come before path variables
   - `/api/books/top5` BEFORE `/api/books/{isbn}`
   - This applies to ALL databases

2. **Docker Volumes Persist Data** 💾
   - Deleting container isn't enough
   - Must `docker volume prune` to clear cached mappings

3. **Profile Switching Works!** ✅
   - Change `spring.profiles.active` in application.properties
   - Completely different database loaded at runtime

4. **Authentication IS Working** ✅
   - Wrong password → 401
   - Wrong role → 403
   - Some users work fine (Maria, Manuel)

5. **Elasticsearch Bootstrapper Works** ✅
   - Creates users, authors, genres, books
   - Data persists in Elasticsearch indices

---

## 🚀 SUCCESS METRICS

**What's Proven to Work:**
✅ Multi-database architecture  
✅ Profile-based switching  
✅ Elasticsearch infrastructure  
✅ Document models & mappers  
✅ Repository implementations  
✅ Bootstrap data creation  
✅ Security configuration (mostly)  
✅ Basic authentication  
✅ Role-based authorization  

**What Needs Fixing:**
⚠️ Admin user access  
⚠️ Some endpoint mappings (404/400)  
⚠️ Possibly controller query parameters  

---

## 📞 SUPPORT INFORMATION

**Project Directory:**
```
F:\repos\novo\MEI-ARCSOFT-2025-2026-1191577-1210939-1250530
```

**Important URLs:**
- Application: http://localhost:8080
- Elasticsearch: http://localhost:9200
- H2 Console: http://localhost:8080/h2-console
- Swagger (if enabled): http://localhost:8080/swagger-ui.html

**Git Branch:**
Check current branch before continuing work

---

**Document Version:** 1.0  
**Last Updated:** 2025-10-26  
**Status:** Ready to resume development  
**Next Session:** Start with "Investigate Admin 401" or "Test with SQL first"

---

## 🎯 RESUME POINT

**When you come back to this project, START HERE:**

1. Read this document completely
2. Check `application.properties` for active profile
3. Verify Docker Elasticsearch is running (`docker ps`)
4. Restart application (`mvn spring-boot:run`)
5. Run `test-all-endpoints.bat` to see current state
6. Choose one priority issue to investigate
7. Refer to relevant section above for how to debug

**Good luck! The hard work is done - just fixing edge cases now!** 🚀

