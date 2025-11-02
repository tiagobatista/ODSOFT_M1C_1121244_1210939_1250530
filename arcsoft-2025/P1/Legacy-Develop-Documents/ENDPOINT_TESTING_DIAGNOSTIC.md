# 🔍 ENDPOINT TESTING - DIAGNOSTIC GUIDE

## Current Status

✅ **Working Endpoint:**
- `GET /api/books/top5` with Maria (LIBRARIAN) → Returns 200 OK

❌ **NOT TESTED YET:**
- Other endpoints with correct credentials

---

## 👤 User Credentials & Roles

### From Bootstrap Data:

| User | Email | Password | Role |
|------|-------|----------|------|
| Admin | admin@gmail.com | AdminPwd1 | LIBRARIAN |
| Maria | maria@gmail.com | Mariaroberta!123 | **LIBRARIAN** |
| Manuel | manuel@gmail.com | Manuelino123! | **READER** |
| João | joao@gmail.com | Joaozinho123! | **READER** |
| Pedro | pedro@gmail.com | Pedrinho123! | **READER** |

⚠️ **IMPORTANT:** Maria is a LIBRARIAN, not a READER!

---

## 🧪 Test Matrix

### Test with Maria (LIBRARIAN)

**Should Work:**
```bash
# Books - LIBRARIAN allowed
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

# Authors - LIBRARIAN allowed
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/authors

# Genres - LIBRARIAN only
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/genres/top5

# Readers - LIBRARIAN allowed
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/readers
```

**Should Fail (403 Forbidden):**
```bash
# Reader-only endpoints
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/suggestions  # READER only
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/authors/{authorNumber}/books  # READER only
```

---

### Test with Manuel (READER)

**Should Work:**
```bash
# Books - READER allowed
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books

# Authors - READER allowed  
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5

# Book suggestions - READER only
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/suggestions
```

**Should Fail (403 Forbidden):**
```bash
# LIBRARIAN-only endpoints
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5  # LIBRARIAN only
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/genres/top5  # LIBRARIAN only
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/readers/top5  # LIBRARIAN only
```

---

## 🔍 Diagnosis Steps

### Step 1: Test with Admin (Should work for everything)
```bash
curl.exe -v -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
```

**Expected:** `HTTP/1.1 200 OK` + JSON data

**If 401 Unauthorized:**
- Application not fully started
- Database not seeded with users
- Password encoding issue

---

### Step 2: Test with Maria (LIBRARIAN)
```bash
curl.exe -v -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

**Expected:** `HTTP/1.1 200 OK` + JSON data

**If 401:**
- Maria's user not created
- Password wrong

**If 403:**
- Role not properly assigned
- SecurityConfig authorization bug

---

### Step 3: Test with Manuel (READER)
```bash
curl.exe -v -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
```

**Expected:** `HTTP/1.1 200 OK` + JSON data

**If 401:**
- Manuel's user not created
- Password wrong

**If 403:**
- Role not properly assigned
- Wrong endpoint chosen (might need LIBRARIAN)

---

## ⚠️ Common Issues

### 401 Unauthorized
**Causes:**
1. Application not fully started (wait 60+ seconds)
2. Wrong password
3. User not created in database
4. Password encoding mismatch

**Solution:**
- Check application logs for "Started PsoftG1Application"
- Verify user was created during bootstrap
- Try with admin@gmail.com first

---

### 403 Forbidden
**Causes:**
1. Correct user, wrong role for endpoint
2. SecurityConfig has wrong hasAuthority()
3. Role not assigned to user

**Solution:**
- Check ENDPOINT_PERMISSIONS.md for required role
- Use correct user (READER vs LIBRARIAN)
- Verify user's role in database

---

### Session Cookie (JSESSIONID)
**What you saw:**
```
--header 'Cookie: JSESSIONID=7A8D0482EE765E63832B74B63300A636'
```

**This means:**
- Session was created (despite STATELESS config)
- Might indicate config issue
- OR might be from H2 Console access

**Not necessarily a problem**, but sessions shouldn't be needed for API.

---

## 🎯 Quick Diagnosis Command

Run this to test all scenarios:

```batch
@echo off
echo Testing Admin (LIBRARIAN)...
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
echo.
echo.

echo Testing Maria (LIBRARIAN)...
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
echo.
echo.

echo Testing Manuel (READER)...
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors
echo.
echo.

echo Testing Wrong Role (Manuel accessing LIBRARIAN endpoint)...
curl.exe -v -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5
echo Expected: 403 Forbidden
echo.
```

---

## 📊 Expected Results Summary

| Endpoint | Manuel (READER) | Maria (LIBRARIAN) |
|----------|----------------|-------------------|
| `GET /api/books` | ✅ 200 | ✅ 200 |
| `GET /api/books/top5` | ❌ 403 | ✅ 200 |
| `GET /api/authors` | ✅ 200 | ✅ 200 |
| `GET /api/authors/top5` | ✅ 200 | ❌ 403 |
| `GET /api/genres/top5` | ❌ 403 | ✅ 200 |
| `GET /api/books/suggestions` | ✅ 200 | ❌ 403 |

---

## 🔧 If Everything Returns 401

**The application probably hasn't finished starting!**

1. Check if Java process is running: `tasklist | findstr java`
2. Wait longer (bootstrap can take 60-90 seconds)
3. Check for "Started PsoftG1Application" in logs
4. Try accessing H2 Console: http://localhost:8080/h2-console

---

## 🚨 Your Previous Test

You said `/api/books/top5` worked with Maria → **This is CORRECT!**
- Maria = LIBRARIAN
- Endpoint requires LIBRARIAN
- ✅ Should return 200 OK

You said `/api/authors` failed (401) with Manuel → **This suggests:**
- Application wasn't ready yet OR
- Manuel's user not created OR  
- Wrong password typed

**Try again now that app has had more time to start!**

---

**Next Step:** Run the quick diagnosis command above to see which endpoints actually work!

