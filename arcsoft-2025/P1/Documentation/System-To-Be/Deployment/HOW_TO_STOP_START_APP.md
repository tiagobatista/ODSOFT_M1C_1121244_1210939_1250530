
## ✋ How to STOP the Application

### Method 1: Graceful Shutdown ⭐ RECOMMENDED
```
1. Go to the terminal where Maven is running
2. Press Ctrl+C
3. Wait for "Application shutdown completed" message
```

### Method 2: Force Kill (If Ctrl+C doesn't work)

**Step 1: Find the Process ID**
```cmd
netstat -ano | findstr :8080
```

**Output example:**
```
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    26776
                                                  ^^^^^ This is the PID
```

**Step 2: Kill the Process**
```cmd
taskkill /F /PID 26776
```
*(Replace 26776 with your actual PID)*

### Method 3: Kill ALL Java Processes ⚠️ WARNING
```cmd
taskkill /F /IM java.exe
```
**WARNING:** This stops ALL Java applications running on your computer!

---

## ▶️ How to START the Application

### Before Starting - Check Status
```cmd
REM Make sure nothing is running on port 8080
netstat -ano | findstr :8080

REM Should return nothing if port is free
```

### Start with SQL + Redis
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

### Start with Elasticsearch
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### What to Watch For ✅
```
INFO ... : The following profiles are active: "sql-redis", "bootstrap"
INFO ... : Bootstrap data creation started
INFO ... : Created 10 books
INFO ... : Created 6 authors  
INFO ... : Created 12 lendings
INFO ... : Started PsoftG1Application in X.XXX seconds
```

---

## 🔄 How to RESTART the Application

### Quick Restart (Same Profile)
```cmd
1. Press Ctrl+C in Maven terminal
2. Wait for shutdown
3. Press ↑ (up arrow) to recall last command
4. Press Enter
```

### Restart with Different Profile
```cmd
1. Stop current app (Ctrl+C)
2. Verify it stopped: netstat -ano | findstr :8080
3. Start with new profile:
   mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

---

## 🩺 Troubleshooting

### "Port 8080 already in use"
```cmd
REM Find what's using the port
netstat -ano | findstr :8080

REM Kill it
taskkill /F /PID <PID>

REM Try starting again
```

### "Application won't stop with Ctrl+C"
```cmd
REM Force kill
taskkill /F /IM java.exe

REM Verify
netstat -ano | findstr :8080
```

### "Can't tell if app is running"
```cmd
REM Check port 8080
netstat -ano | findstr :8080

REM If you see output -> App is RUNNING
REM If no output -> App is STOPPED
```

### "Bootstrap didn't create data"
```cmd
REM 1. Stop the app
REM 2. Check you included 'bootstrap' in the profile:
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
                                                           ^^^^^^^^^
                                                           IMPORTANT!
```

---

## 📋 Complete Workflow Example

```cmd
REM ====================
REM INITIAL SETUP
REM ====================

REM 1. Start Redis
docker run -d --name redis -p 6379:6379 redis:latest

REM 2. Verify Redis is running
docker ps | findstr redis

REM 3. Make sure port 8080 is free
netstat -ano | findstr :8080

REM ====================
REM START APPLICATION
REM ====================

REM 4. Start app (keep this terminal open)
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false

REM 5. Watch for bootstrap messages
REM    "Created X books"
REM    "Created X lendings"

REM ====================
REM TEST (in a NEW terminal)
REM ====================

REM 6. Test the app
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books

REM Should return a list of books!

REM ====================
REM STOP APPLICATION
REM ====================

REM 7. Go back to Maven terminal and press Ctrl+C

REM 8. Verify it stopped
netstat -ano | findstr :8080
REM (Should show nothing)
```

---

## 🎯 Your Current Situation

**App is running on PID 26776**
```cmd
REM To stop it:
taskkill /F /PID 26776

REM Then start correctly:
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

---

## ⚡ Speed Tips

### Keep Maven Terminal Open
- Don't close the Maven terminal
- Makes stopping/restarting easier
- Can see logs in real-time

### Use Command History
- Press ↑ (up arrow) to recall previous commands
- Saves typing the long mvn command repeatedly

### Open Multiple Terminals
- Terminal 1: Run Maven (keep open)
- Terminal 2: Run curl tests
- Terminal 3: Check Docker, netstat, etc.

### Create Batch Files
Save frequently used commands:

**start-app.bat:**
```bat
@echo off
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**stop-app.bat:**
```bat
@echo off
netstat -ano | findstr :8080
echo.
echo Enter the PID to kill (number from above):
set /p pid=
taskkill /F /PID %pid%
```

---

## 📚 Related Documentation

- **Full Quick Start:** `01-Quick-Start-Guide.md`
- **Redis Testing:** `02-Redis-Caching-Test-Guide.md`
- **Elasticsearch Testing:** `03-Elasticsearch-Test-Guide.md`
- **Complete Strategy:** `URGENT_DELIVERY_STRATEGY.md`

---

**Remember:** Always use **Ctrl+C** first! Only force-kill if absolutely necessary.

