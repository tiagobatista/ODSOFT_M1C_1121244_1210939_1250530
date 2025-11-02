anu# ✅ CORRECT BOOTSTRAP CREDENTIALS

## 🔴 IMPORTANT - Wrong Password in Documentation!

The documentation said `Password1` but that's **WRONG**!

## ✅ Correct Bootstrap Users

### READERS (Role: READER)

#### Reader 1 - Manuel
```
Username: manuel@gmail.com
Password: Manuelino123!
Role: READER
```

#### Reader 2 - João
```
Username: joao@gmail.com
Password: Joaoratao!123
Role: READER
```

#### Reader 3 - Pedro
```
Username: pedro@gmail.com
Password: Pedrodascenas!123
Role: READER
```

#### Reader 4 - Catarina
```
Username: catarina@gmail.com
Password: Catarinamartins!123
Role: READER
```

#### Reader 5 - Marcelo
```
Username: marcelo@gmail.com
Password: Marcelosousa!123
Role: READER
```

#### Reader 6 - Luís
```
Username: luis@gmail.com
Password: Luismontenegro!123
Role: READER
```

### LIBRARIAN (Role: LIBRARIAN)

#### Librarian - Maria
```
Username: maria@gmail.com
Password: Mariaroberta!123
Role: LIBRARIAN
```

---

## 🎭 Endpoint Permissions - IMPORTANT!

### Books Endpoints - Who Can Access What?

#### ✅ READER (Manuel) Can Access:
```
GET  /api/books                    ✅ List all books
GET  /api/books/{isbn}             ✅ Get specific book
GET  /api/books/{isbn}/photo       ✅ Get book photo
GET  /api/books/suggestions        ✅ Get book suggestions
POST /api/books/search             ✅ Search books
```

#### ❌ READER (Manuel) CANNOT Access (403 Forbidden):
```
GET  /api/books/top5               ❌ Requires LIBRARIAN
GET  /api/books/{isbn}/avgDuration ❌ Requires LIBRARIAN
PUT  /api/books/{isbn}             ❌ Requires LIBRARIAN
PATCH /api/books/{isbn}            ❌ Requires LIBRARIAN
DELETE /api/books/{isbn}/photo     ❌ Requires LIBRARIAN
```

#### ✅ LIBRARIAN (Maria) Can Access:
```
ALL book endpoints - Full access!
```

---

## 🚀 cURL Commands for Postman Import

### ✅ Test Get Books (Works with READER)
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### ✅ Test Get Books (Works with LIBRARIAN)
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

### ❌ Test Top 5 Books (LIBRARIAN ONLY - Manuel will get 403)
```bash
# This will FAIL with Manuel (403 Forbidden)
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5

# This will WORK with Maria
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

### Test Login Endpoint
```bash
curl -X POST \
  http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manuel@gmail.com",
    "password": "Manuelino123!"
  }'
```

### Test Get Overdue Lendings (LIBRARIAN only)
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/lendings/overdue
```

### Test Get Top 5 Readers (LIBRARIAN only)
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/readers/top5
```

---

## 📋 For Postman

### Quick Setup - Basic Auth

**For READER endpoints:**
```
Authorization: Basic Auth
Username: manuel@gmail.com
Password: Manuelino123!
```

**For LIBRARIAN endpoints:**
```
Authorization: Basic Auth
Username: maria@gmail.com
Password: Mariaroberta!123
```

### Example Request in Postman

1. **GET Books**
   - URL: `http://localhost:8080/api/books`
   - Method: GET
   - Authorization:
     - Type: Basic Auth
     - Username: `manuel@gmail.com`
     - Password: `Manuelino123!`

2. **POST Login**
   - URL: `http://localhost:8080/api/public/login`
   - Method: POST
   - Headers:
     - Content-Type: `application/json`
   - Body (raw JSON):
     ```json
     {
       "username": "manuel@gmail.com",
       "password": "Manuelino123!"
     }
     ```

---

## 🔍 Verify in H2 Console

```sql
-- Check all users
SELECT id, username, enabled FROM USER_ENTITY;

-- Check user details
SELECT * FROM USER_ENTITY WHERE username = 'manuel@gmail.com';

-- Check reader details
SELECT * FROM READER_DETAILS_ENTITY;
```

---

## ⚡ Quick Test Commands

Copy and paste these into your terminal:

### Test 1: Get Books as Reader
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### Test 2: Get Books as Librarian
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

### Test 3: Login and Get Token
```bash
curl -X POST http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manuel@gmail.com","password":"Manuelino123!"}' \
  -v
```

Look for the `Authorization:` header in the response - that's your JWT token!

### Test 4: Use JWT Token
```bash
# First get the token from Test 3, then:
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" http://localhost:8080/api/books
```

---

## 🎯 Complete Working Example

### Step 1: Login
```bash
curl -X POST http://localhost:8080/api/public/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manuel@gmail.com","password":"Manuelino123!"}'
```

**Expected Response:**
```json
{
  "id": 1,
  "username": "manuel@gmail.com",
  "fullName": "Manuel Sarapinto das Coives",
  ...
}
```

**Check Headers for:**
```
Authorization: eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJleGFt...
```

### Step 2: Use Basic Auth to Get Books
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### Step 3: Get Reader's Own Lendings
```bash
curl -u manuel@gmail.com:Manuelino123! \
  http://localhost:8080/api/readers/2025/1/lendings
```

### Step 4: Librarian Action - Get Overdue
```bash
curl -u maria@gmail.com:Mariaroberta!123 \
  http://localhost:8080/api/lendings/overdue
```

---

## 📝 Summary Table

| Username | Password | Role | Can Access |
|----------|----------|------|------------|
| manuel@gmail.com | Manuelino123! | READER | Books, Authors, Own profile |
| joao@gmail.com | Joaoratao!123 | READER | Books, Authors, Own profile |
| pedro@gmail.com | Pedrodascenas!123 | READER | Books, Authors, Own profile |
| catarina@gmail.com | Catarinamartins!123 | READER | Books, Authors, Own profile |
| marcelo@gmail.com | Marcelosousa!123 | READER | Books, Authors, Own profile |
| luis@gmail.com | Luismontenegro!123 | READER | Books, Authors, Own profile |
| maria@gmail.com | Mariaroberta!123 | LIBRARIAN | Everything + Reports |

---

## ⚠️ Common Mistakes

### ❌ Wrong Password Format
```
Password1          ← WRONG! This was in the docs but is incorrect
Manuelino123!      ← CORRECT! This is the actual password
```

### ❌ Missing Special Characters
The passwords have **exclamation marks** at the end - don't forget them!

### ❌ Case Sensitive
Usernames are case-sensitive. Use lowercase.

---

## ✅ This Should Work Now!

Try this exact command:

```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

If this **still** doesn't work, the issue might be:
1. Application not running
2. Different bootstrap data
3. Database was reset

Check H2 console to verify users exist:
```
http://localhost:8080/h2-console
SQL: SELECT username FROM USER_ENTITY;
```

---

**Status: Ready to test! 🚀**

