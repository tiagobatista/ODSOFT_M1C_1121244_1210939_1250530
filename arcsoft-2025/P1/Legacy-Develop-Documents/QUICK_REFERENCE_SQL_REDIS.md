# Quick Reference - SQL + Redis Application

## 🚀 Quick Start

```cmd
# Start with UTF-8 support (emojis display correctly)
start-redis-test-utf8.bat

# OR standard start (garbled emojis, but works fine)
start-redis-test.bat
```

## ✅ Application Status Check

```cmd
# Check if running on port 8080
netstat -ano | findstr :8080

# Expected output:
#   TCP    0.0.0.0:8080    LISTENING    [PID]
```

## 🧪 Quick Test Commands

### Using cURL (PowerShell/CMD)
```cmd
# Test top 5 books (use curl.exe in PowerShell!)
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

# Test top 5 authors
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5

# Test top 5 genres
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/genres/top5
```

### Using Postman ✅ TESTED & WORKING
1. **Method:** GET
2. **URL:** `http://localhost:8080/api/books/top5`
3. **Authorization Tab:**
   - Type: **Basic Auth**
   - Username: `maria@gmail.com`
   - Password: `Mariaroberta!123`
4. **Click Send** → Should get **200 OK** with book data

**Other endpoints to test:**
- `GET /api/authors/top5` (use manuel@gmail.com / Manuelino123!)
- `GET /api/genres/top5` (use maria@gmail.com / Mariaroberta!123)

## 📊 Understanding Logs

| Symbol (Garbled) | Emoji | Meaning |
|------------------|-------|---------|
| `Γ¥î` | ❌ | CACHE MISS - Reading from SQL |
| `≡ƒÄ»` | 📚 | CACHE HIT - Found in Redis |
| `≡ƒÆ╛` | 💾 | Saved to SQL database |
| `ΓÖ╗∩╕Å` | 🔄 | Updated Redis cache |

## ⏱️ Startup Timeline

| Event | Time | What's Happening |
|-------|------|------------------|
| Maven compiling | 0-10s | Building project |
| Spring initializing | 10-20s | Loading beans |
| Bootstrap starting | 20s-2m | Creating test data |
| **App ready** | ~2m | Look for "Started PsoftG1Application" |

## 🛠️ Common Issues & Fixes

### Issue: Garbled Characters
**Fix:** Use `start-redis-test-utf8.bat` or run `chcp 65001` first

### Issue: "No bean of type UserRepository"
**Fix:** Profile must be `sql-redis`, not `sql`
- Check `application.properties`: `spring.profiles.active=sql-redis,bootstrap`

### Issue: Port 8080 already in use
**Fix:** Kill existing process
```cmd
netstat -ano | findstr :8080
taskkill /F /PID [PID_NUMBER]
```

### Issue: Application appears frozen
**Fix:** Wait! It's creating bootstrap data (50 lendings = 1-2 minutes)

## 🔄 Switching Databases

### To Elasticsearch
```properties
# application.properties
spring.profiles.active=elasticsearch,bootstrap
```
Then run: `restart-elasticsearch.ps1`

### Back to SQL+Redis
```properties
# application.properties
spring.profiles.active=sql-redis,bootstrap
```

## 📁 Key Files

| File | Purpose |
|------|---------|
| `CONSOLE_ENCODING_GUIDE.md` | Full encoding issue explanation |
| `SQL_REDIS_STATUS_REPORT.md` | Complete status report |
| `start-redis-test-utf8.bat` | UTF-8 enabled startup |
| `application.properties` | Configuration (must have `sql-redis` profile) |

## 🔐 Test Users

| Email | Password | Role |
|-------|----------|------|
| `admin@gmail.com` | `AdminPwd1` | ADMIN |
| `maria@gmail.com` | `Mariaroberta!123` | LIBRARIAN |
| `manuel@gmail.com` | `Manuelino123!` | READER |

## 📌 Remember

✅ **App is NOT hanging** - bootstrap takes 1-2 minutes  
✅ **Garbled emojis are COSMETIC** - functionality works fine  
✅ **Use curl.exe** in PowerShell (not curl alias)  
✅ **Wait for "Started PsoftG1Application"** before testing  
✅ **Second requests are faster** - caching works!  

