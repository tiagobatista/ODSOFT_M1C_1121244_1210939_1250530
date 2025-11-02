# ⚡ QUICK START - FIX THE BOOTSTRAP ISSUE NOW!

## 🚨 THE PROBLEM
Your app is running **WITHOUT** bootstrap data - that's why you're getting empty responses!

## ✅ THE SOLUTION (5 Minutes)

### Step 1: Stop Current App
```cmd
REM Find the process
netstat -ano | findstr :8080

REM Kill it (use the PID from the output)
taskkill /F /PID <PID>

REM Verify it stopped
netstat -ano | findstr :8080
REM (Should show nothing)
```

### Step 2: Start App CORRECTLY

**⚠️ IMPORTANT: You're using PowerShell, so you MUST add `--%` before spring-boot:run**

```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**Why the `--%`?**
- PowerShell interprets `-D` as a parameter
- `--% ` tells PowerShell to stop parsing and pass everything to Maven literally
- **Without it, Maven will fail!**

### Step 3: Watch for Bootstrap Logs

You should see:
```
INFO ... : The following profiles are active: "sql-redis", "bootstrap"
INFO ... : Bootstrap data creation started
INFO ... : Created 10 books
INFO ... : Created 6 authors
INFO ... : Created 12 lendings
INFO ... : Started PsoftG1Application in X.XXX seconds
```

### Step 4: Test Immediately

**Open a NEW terminal** (keep Maven running) and test:

```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

**Expected:** Should return a JSON array with 10 books!

---

## 🎯 ALTERNATIVE: Use CMD Instead of PowerShell

If you prefer, you can use CMD (Command Prompt) instead - it doesn't need the `--%`:

```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

---

## 📚 Full Documentation

All guides are now in:
```
arcsoft-2025/P1/Documentation/System-To-Be/Deployment/
```

Files:
- `01-Quick-Start-Guide.md` - Complete setup instructions
- `02-Redis-Caching-Test-Guide.md` - Redis testing procedures
- `03-Elasticsearch-Test-Guide.md` - Elasticsearch testing
- `HOW_TO_STOP_START_APP.md` - Detailed start/stop reference
- `README.md` - Index of all guides

---

## ⏰ YOU HAVE 6 HOURS - HERE'S YOUR TIMELINE

1. **NOW (10 min):** Fix bootstrap and verify data loads
2. **Hour 1 (60 min):** Test Redis caching thoroughly
3. **Hour 2 (60 min):** Test Elasticsearch
4. **Hours 3-4 (120 min):** Update report with screenshots
5. **Hour 5 (45 min):** Final review and testing
6. **Hour 6 (15 min):** Submit and prepare backup

---

## 🚀 DO THIS NOW!

```powershell
# 1. Stop app
taskkill /F /IM java.exe

# 2. Verify Redis running
docker ps | findstr redis

# 3. Start correctly (note the --%)
mvn --% spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false

# 4. Wait for "Started PsoftG1Application"

# 5. In NEW terminal, test:
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

**YOU GOT THIS! 💪**

