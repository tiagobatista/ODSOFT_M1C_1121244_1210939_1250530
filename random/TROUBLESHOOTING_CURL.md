# 🔧 Troubleshooting Guide - API Not Working

## Issue: curl commands not working

If both of these are failing:
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

Let's systematically troubleshoot the issue.

---

## Step 1: Verify Application is Running

### Check if port 8080 is active:

**Windows PowerShell:**
```powershell
netstat -ano | findstr :8080
```

**Expected output:**
```
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    <PID>
```

If you see nothing, the application is **NOT running**. Start it with:
```bash
mvn spring-boot:run
```

---

## Step 2: Test the Public Endpoint First

This should work WITHOUT authentication:

```bash
curl http://localhost:8080/h2-console
```

**Expected:** You should get HTML response (H2 console page)

If this fails with "Connection refused" → Application is not running

---

## Step 3: Test Login Endpoint (Public, No Auth Required)

```bash
curl -X POST http://localhost:8080/api/public/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"manuel@gmail.com\",\"password\":\"Manuelino123!\"}"
```

**Expected:** JSON response with user details + Authorization header

**Common Issues:**

### If you get "Connection refused"
→ Application is not running

### If you get 404 Not Found
→ Endpoint doesn't exist, check URL

### If you get 400 Bad Request
→ JSON syntax error (PowerShell escaping issue)

---

## Step 4: Fix curl Command for Windows

Windows cmd/PowerShell has different syntax. Here are the correct commands:

### For Windows Command Prompt (cmd):
```cmd
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### For Windows PowerShell:
```powershell
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### If curl is aliased to Invoke-WebRequest (PowerShell issue):
```powershell
# Remove alias temporarily
Remove-Item alias:curl

# Then try again
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### Alternative - Use curl.exe explicitly:
```powershell
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

---

## Step 5: Try with Verbose Output

This will show you exactly what's happening:

```bash
curl -v -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

Look for:
- `< HTTP/1.1 200 OK` → Success
- `< HTTP/1.1 401 Unauthorized` → Auth problem
- `< HTTP/1.1 403 Forbidden` → Permission problem
- `< HTTP/1.1 404 Not Found` → Wrong URL
- `Connection refused` → App not running

---

## Step 6: Test in Postman Instead

If curl is giving you trouble, use Postman:

1. **New Request**
   - Method: GET
   - URL: `http://localhost:8080/api/books`

2. **Authorization Tab**
   - Type: Basic Auth
   - Username: `manuel@gmail.com`
   - Password: `Manuelino123!`

3. **Send**

---

## Step 7: Check Application Logs

Look at your terminal where `mvn spring-boot:run` is running.

**Good signs:**
```
Started PsoftG1Application in X.XXX seconds
Tomcat started on port 8080
```

**Bad signs:**
```
APPLICATION FAILED TO START
Port 8080 was already in use
```

---

## Step 8: Verify Data Exists

Open H2 Console in browser:
```
http://localhost:8080/h2-console
```

Run this SQL:
```sql
-- Check if books exist
SELECT COUNT(*) FROM BOOK_ENTITY;

-- Check if users exist
SELECT username FROM USER_ENTITY;
```

If counts are 0, bootstrap might not have run.

---

## Step 9: Common Windows curl Issues

### Issue: Special characters in password

The `!` in `Manuelino123!` might cause issues in some shells.

**Try with quotes:**
```bash
curl -u "manuel@gmail.com:Manuelino123!" http://localhost:8080/api/books
```

### Issue: PowerShell alias

PowerShell has `curl` as an alias to `Invoke-WebRequest`.

**Solution 1 - Use curl.exe:**
```powershell
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

**Solution 2 - Use Invoke-RestMethod:**
```powershell
$user = "manuel@gmail.com"
$pass = "Manuelino123!"
$pair = "$user:$pass"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{ Authorization = "Basic $base64" }

Invoke-RestMethod -Uri http://localhost:8080/api/books -Headers $headers
```

---

## Step 10: Working Examples for Different Shells

### Git Bash (Windows):
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### Command Prompt (cmd):
```cmd
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### PowerShell (use curl.exe):
```powershell
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### WSL/Linux:
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

---

## Most Likely Issues

1. **Application not running** (80% of cases)
   - Solution: `mvn spring-boot:run`

2. **PowerShell curl alias** (15% of cases)
   - Solution: Use `curl.exe` instead

3. **Special character escaping** (4% of cases)
   - Solution: Use quotes around credentials

4. **Wrong endpoint/typo** (1% of cases)
   - Solution: Double-check URL

---

## Quick Diagnostic Command

Run this to check everything at once:

```bash
# Check if app is running
netstat -ano | findstr :8080

# Test public endpoint
curl http://localhost:8080/h2-console

# Test with verbose
curl.exe -v -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

---

## What Error Are You Getting?

### "Connection refused"
→ **Application is not running**
→ Start with: `mvn spring-boot:run`

### "401 Unauthorized"
→ **Credentials are wrong**
→ Verify in H2 console: `SELECT username FROM USER_ENTITY;`

### "403 Forbidden"
→ **User doesn't have permission**
→ Try with Maria (LIBRARIAN): `curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books`

### "404 Not Found"
→ **URL is wrong**
→ Check it's `/api/books` not `/books`

### No output / hangs
→ **curl might be PowerShell alias**
→ Use `curl.exe` instead

---

## Final Working Command (for Windows)

**This should definitely work:**

```powershell
curl.exe -v -u "manuel@gmail.com:Manuelino123!" http://localhost:8080/api/books
```

Copy this EXACTLY and paste it in PowerShell.

---

## Still Not Working?

**Tell me:**
1. What shell are you using? (cmd, PowerShell, Git Bash, etc.)
2. What exact error message do you see?
3. Is the application running? (check with `netstat -ano | findstr :8080`)
4. Can you access `http://localhost:8080/h2-console` in browser?

Then I can give you the exact solution!

