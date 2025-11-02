# Console Encoding Issues - Windows Terminal

## Problem

When running the SQL+Redis application, you may see garbled characters in the console like:
```
Γ¥î CACHE MISS
≡ƒÄ» CACHE HIT
≡ƒÆ╛ Saved to SQL
ΓÖ╗∩╕Å Updated Redis cache
```

## Root Cause

The Redis cache repository classes use **UTF-8 emoji characters** for better log visualization:
- ❌ (Red X) = CACHE MISS - Fetching from SQL
- 📚 (Books) = CACHE HIT - Found in Redis cache
- 💾 (Floppy Disk) = Saved to SQL database
- 🔄 (Refresh) = Updated Redis cache

Windows Command Prompt by default uses **code page 850** or **Windows-1252** encoding, which cannot display UTF-8 emojis correctly.

## Solution

### Option 1: Use UTF-8 Enabled Batch File (Recommended)

Run the application using the UTF-8 configured script:
```cmd
start-redis-test-utf8.bat
```

This script automatically sets the console encoding to UTF-8 before starting the application.

### Option 2: Manually Set Console Encoding

Before running the application, set the console code page to UTF-8:
```cmd
chcp 65001
mvn spring-boot:run
```

### Option 3: Use Windows Terminal (Best Experience)

Windows Terminal has better UTF-8 support than Command Prompt:
1. Install Windows Terminal from Microsoft Store (if not already installed)
2. Run the batch file from Windows Terminal
3. Emojis will display correctly

## Is the Application Hanging?

**No!** The application is **NOT hanging**. What you're seeing is:

### Bootstrap Process
The logs showing "Lending: 2025/45", "Lending: 2025/46", etc. indicate the **bootstrap process is creating test data**:
- Creating 50 lendings
- Creating books, authors, genres
- Populating both SQL database and Redis cache

This process typically takes **1-2 minutes** depending on your system.

### What to Expect

The bootstrap sequence:
```
1. Creating users (admin, librarians, readers) ✓
2. Creating forbidden names ✓
3. Creating genres ✓
4. Creating authors ✓
5. Creating books ✓
6. Creating lendings (this takes the longest - you'll see many CACHE MISS logs) ⏳
7. Application started - Ready for requests ✅
```

### How to Know It's Done

Look for this final message:
```
Started PsoftG1Application in X.XXX seconds
```

Then you'll see:
```
✅ Elasticsearch bootstrapping completed!
```

or for SQL+Redis:
```
✅ Bootstrap data creation completed!
```

## Performance Notes

### First Run (Cold Start)
- All reads result in **CACHE MISS** (❌)
- Data is fetched from SQL and **cached in Redis**
- Takes longer due to database + cache population

### Subsequent Reads (Warm Cache)
- Reads result in **CACHE HIT** (📚)
- Data retrieved directly from Redis (much faster)
- No database query needed

### Cache Behavior You'll See

**Pattern during bootstrap:**
```
❌ CACHE MISS - Fetching from SQL - Book ISBN: 9789723716160
💾 Saved to SQL - Book ISBN: 9789723716160
🔄 Updated Redis cache - Book ISBN: 9789723716160
... later ...
📚 CACHE HIT - Book ISBN: 9789723716160 (Redis returned the cached value!)
```

This shows Redis caching is **working correctly**.

## Testing the Application

Once you see "Started PsoftG1Application", you can test endpoints:

### Test Cache Performance
```cmd
REM First request (cache miss - slower)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

REM Second request (cache hit - faster!)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

Check the logs - the second request should show more **CACHE HIT** messages.

## Summary

| Issue | Status | Solution |
|-------|--------|----------|
| Garbled emoji characters | Encoding mismatch | Use `start-redis-test-utf8.bat` or `chcp 65001` |
| Application appears to hang | Bootstrap data creation | Wait 1-2 minutes for bootstrap to complete |
| Many CACHE MISS logs | Expected behavior | First reads populate the cache |
| Application startup time | Normal | SQL + Redis + Bootstrap = 1-2 minutes |

## Quick Reference

**To run the app with proper encoding:**
```cmd
start-redis-test-utf8.bat
```

**To check if the app is ready:**
Look for: `Started PsoftG1Application in X.XXX seconds`

**To test endpoints:**
Wait for startup message, then use curl commands or Postman.

