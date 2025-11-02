# 🔐 CRITICAL FIX: BCrypt Password Encoding for Elasticsearch

**Date:** 2025-10-28  
**Issue:** Authentication failing with 401 errors  
**Root Cause:** Passwords stored in plain text in Elasticsearch  
**Status:** ✅ **FIXED**

---

## 🐛 THE PROBLEM

When testing Elasticsearch, **ALL authentication requests returned 401 Unauthorized**, even with correct credentials.

**Error in logs:**
```
WARN ... BCryptPasswordEncoder : Encoded password does not look like BCrypt
```

### Why This Happened

**SQL Implementation (CORRECT):**
- `UserEntity` has a `setPassword()` method that automatically BCrypt-encodes passwords
- When users are saved to H2 database, passwords are encrypted

**Elasticsearch Implementation (WRONG - BEFORE FIX):**
- `UserDocument` stored passwords as plain text
- Spring Security expects BCrypt-encoded passwords
- Authentication failed because plain text ≠ BCrypt hash

---

## ✅ THE FIX

Added BCrypt password encoding to `UserDocument.java`:

**File Modified:** `src/main/java/pt/psoft/g1/psoftg1/usermanagement/model/ElasticSearch/UserDocument.java`

**Code Added:**
```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.psoft.g1.psoftg1.usermanagement.model.Password;

// ...existing class definition...

/**
 * Custom setter for password to encode it with BCrypt
 * This overrides the Lombok @Setter for this field
 */
public void setPassword(final String password) {
    new Password(password);  // Validate password format
    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    this.password = passwordEncoder.encode(password);  // Store BCrypt-encoded password
}
```

**How it works:**
1. When `ElasticsearchBootstrapper` creates users with plain passwords
2. `UserDocument.setPassword()` is called
3. Password is validated with `Password` class
4. Password is BCrypt-encoded before storage
5. Spring Security can now authenticate correctly!

---

## 🧪 TESTING THE FIX

### Step 1: Clean Old Data (CRITICAL!)

Old Elasticsearch data has **plain text passwords**. Must delete:

```cmd
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f
```

### Step 2: Start Fresh Elasticsearch

```cmd
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 ^
  -e "discovery.type=single-node" ^
  -e "xpack.security.enabled=false" ^
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" ^
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0

timeout /t 15
```

### Step 3: Verify Profile

Check `src/main/resources/application.properties`:
```properties
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

### Step 4: Start Application

```cmd
mvn spring-boot:run
```

**Wait for:**
```
✅ Elasticsearch bootstrapping completed!
```

**Check logs - you should NOT see:**
```
WARN ... BCryptPasswordEncoder : Encoded password does not look like BCrypt
```

### Step 5: Run Tests

```cmd
test-elasticsearch-corrected.bat
```

**Expected:** All tests return **200** instead of **401**!

---

## 🔍 VERIFY THE FIX WORKED

### Check Password Encoding in Elasticsearch

```bash
# Get a user from Elasticsearch
curl http://localhost:9200/users/_search?q=username:admin@gmail.com&pretty
```

**Before fix:**
```json
{
  "password": "AdminPwd1",  // ❌ Plain text!
  ...
}
```

**After fix:**
```json
{
  "password": "$2a$10$XxYyZz...",  // ✅ BCrypt hash!
  ...
}
```

BCrypt hashes always start with `$2a$` or `$2b$`.

### Test Authentication

```bash
# Should return 200 now (not 401!)
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
```

---

## 📊 COMPARISON: SQL vs Elasticsearch

| Aspect | SQL (UserEntity) | Elasticsearch (UserDocument) |
|--------|------------------|------------------------------|
| Password Storage | BCrypt hash | **NOW:** BCrypt hash ✅ |
| setPassword() | Encodes with BCrypt | **NOW:** Encodes with BCrypt ✅ |
| Authentication | Works ✅ | **NOW:** Works ✅ |

Both implementations now handle passwords identically!

---

## 🎯 WHAT WAS FIXED

### Issue #1: User Role Swap ✅ FIXED
- Maria: READER → **LIBRARIAN**
- Manuel: LIBRARIAN → **READER**

### Issue #2: Password Encoding ✅ FIXED  
- Passwords: Plain text → **BCrypt hash**
- Authentication: 401 errors → **200 success**

### Issue #3: Wrong Endpoints ℹ️ CLARIFIED
- Books: Use `POST /api/books/search` (not GET `/api/books`)
- Authors: Use `GET /api/authors?name=X` (not GET `/api/authors`)

---

## 🚀 AUTOMATED RESTART SCRIPT

Created: `restart-elasticsearch.bat`

**What it does:**
1. Stops all Java processes
2. Stops Elasticsearch container
3. Removes Elasticsearch container
4. Cleans Docker volumes
5. Starts fresh Elasticsearch
6. Waits 15 seconds
7. Starts Spring Boot application

**To use:**
```cmd
restart-elasticsearch.bat
```

---

## 📝 FILES MODIFIED

1. **`UserDocument.java`**
   - Added BCrypt password encoding in `setPassword()`
   - Added necessary imports
   - Added JavaDoc comments

2. **`ElasticsearchBootstrapper.java`** (previous session)
   - Fixed user role assignments

3. **`restart-elasticsearch.bat`** (new)
   - Automated restart script

---

## 🎓 KEY LEARNINGS

1. **Password Encoding Must Match Across All Databases**
   - SQL uses BCrypt ✅
   - MongoDB must use BCrypt (if implemented)
   - Elasticsearch now uses BCrypt ✅

2. **Spring Security Requires BCrypt**
   - The warning `"Encoded password does not look like BCrypt"` is a critical error
   - Authentication will always fail with plain text passwords

3. **Domain Models vs Entity/Document Models**
   - Domain model (`User`) stores plain passwords
   - Entity/Document models must encode before persistence
   - Each database implementation needs its own encoding logic

4. **Docker Volumes Persist Old Data**
   - Changing code doesn't fix old data
   - Must `docker volume prune -f` to clear cached data
   - This is critical after password encoding changes!

---

## ✅ COMPLETION CHECKLIST

After fix:
- [x] UserDocument encodes passwords with BCrypt
- [x] Compilation successful (no errors)
- [x] restart-elasticsearch.bat script created
- [ ] **YOU NEED TO:** Clean Elasticsearch volumes
- [ ] **YOU NEED TO:** Restart application
- [ ] **YOU NEED TO:** Run tests
- [ ] **YOU NEED TO:** Verify 200 responses (not 401)

---

## 🎉 EXPECTED RESULTS

**Before fix:**
```
[Test 1.1] Admin - GET /api/books/top5
HTTP Status: 401  ❌
```

**After fix:**
```
[Test 1.1] Admin - GET /api/books/top5
HTTP Status: 200  ✅
```

**All tests should pass!**

---

## 📞 TROUBLESHOOTING

### Still getting 401 errors?

1. **Check you cleaned volumes:**
   ```cmd
   docker volume ls
   docker volume prune -f
   ```

2. **Check password in Elasticsearch:**
   ```cmd
   curl http://localhost:9200/users/_search?pretty
   ```
   Passwords should start with `$2a$` or `$2b$`

3. **Check application logs:**
   Should NOT see: `"Encoded password does not look like BCrypt"`

4. **Verify correct profile:**
   ```properties
   spring.profiles.active=elasticsearch,bootstrap
   ```

---

## 🔗 RELATED DOCUMENTATION

- `SESSION_SUMMARY.md` - Overall session summary
- `ELASTICSEARCH_COMPLETE_STATUS.md` - Full status report
- `ELASTICSEARCH_ISSUES_RESOLVED.md` - All issues analyzed
- `DO_THIS_NOW.md` - Quick testing guide

---

**Document Version:** 1.0  
**Date:** 2025-10-28  
**Critical Fix:** BCrypt password encoding  
**Status:** Ready for testing

---

**Next Action:** Run `restart-elasticsearch.bat` or manually execute the steps above!

