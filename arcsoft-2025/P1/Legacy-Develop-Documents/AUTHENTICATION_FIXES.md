# ✅ FIXED - Authentication & Endpoints Issues

## Issues Found & Resolved

### ❌ Issue 1: Swagger UI Not Working
**Problem:** You were trying to access `/swagger-ui.html`  
**Solution:** The correct path is `/swagger-ui/index.html`

**Correct URL:**
```
http://localhost:8080/swagger-ui/index.html
```

---

### ❌ Issue 2: 401 Unauthorized on API Endpoints
**Problem:** All API endpoints require authentication  
**Solution:** Use Basic Auth or JWT tokens

**Quick Fix for Postman:**
1. Open any request (e.g., `GET http://localhost:8080/api/books`)
2. Go to **Authorization** tab
3. Type: **Basic Auth**
4. Username: `manuel@gmail.com`
5. Password: `Password1`
6. Send request → Should work now! ✅

---

## 🔑 Authentication Summary

### What You Need to Know

1. **H2 Console** = ✅ Works (no auth required)
2. **Swagger UI** = ✅ Works at `/swagger-ui/index.html` (no auth for UI, but needs auth to test endpoints)
3. **API Endpoints** = ❌ Require authentication

### Security Architecture

The application uses **Spring Security** with:
- ✅ JWT tokens for stateless auth
- ✅ HTTP Basic Auth as alternative
- ✅ Role-based access control (READER, LIBRARIAN, ADMIN)

### Public Endpoints (No Auth Required)

```
POST /api/public/login       ← Login endpoint
POST /api/readers            ← Register new reader
GET  /h2-console/**          ← Database console
GET  /swagger-ui/**          ← API documentation
GET  /api-docs/**            ← OpenAPI spec
```

### Protected Endpoints (Auth Required)

```
/api/books/**                ← Books management
/api/authors/**              ← Authors management
/api/readers/**              ← Readers management
/api/lendings/**             ← Lendings management
/api/genres/**               ← Genres and reports
```

---

## 🚀 Quick Start Guide

### 1. Check Available Users

Open H2 Console:
```
http://localhost:8080/h2-console
```

Run this SQL:
```sql
SELECT username, enabled FROM USER_ENTITY;
```

You should see bootstrap users like:
- `manuel@gmail.com` (READER)
- Possibly a LIBRARIAN user

---

### 2. Test Login

**In Postman:**
```
POST http://localhost:8080/api/public/login
Content-Type: application/json

Body (raw JSON):
{
  "username": "manuel@gmail.com",
  "password": "Password1"
}
```

**Response:** JWT token in `Authorization` header + user details

---

### 3. Use Basic Auth for Testing

**In Postman (Recommended for Development):**

For ANY API request:
1. URL: `http://localhost:8080/api/books`
2. Method: `GET`
3. **Authorization** tab:
   - Type: **Basic Auth**
   - Username: `manuel@gmail.com`
   - Password: `Password1`
4. Send → ✅ Should work!

---

### 4. Use Swagger UI with Auth

1. Open: http://localhost:8080/swagger-ui/index.html
2. Click **Authorize** button (🔒 top right)
3. Enter:
   - Username: `manuel@gmail.com`
   - Password: `Password1`
4. Click **Authorize** then **Close**
5. Now test any endpoint with "Try it out"!

---

## 📋 Postman Collection Setup

### Option 1: Per-Request Auth
Set authorization on each request individually.

### Option 2: Collection-Level Auth (Recommended)
1. Create new collection: "Library API"
2. Click on collection name
3. Go to **Authorization** tab
4. Type: **Basic Auth**
5. Username: `manuel@gmail.com`
6. Password: `Password1`
7. Save

**Now all requests in this collection inherit the auth automatically!**

---

## 🎯 Example API Calls

### Get All Books (READER role)
```http
GET http://localhost:8080/api/books
Authorization: Basic manuel@gmail.com:Password1
```

### Get Specific Book
```http
GET http://localhost:8080/api/books/978-1-234-56789-0
Authorization: Basic manuel@gmail.com:Password1
```

### Get Top 5 Readers (LIBRARIAN role)
```http
GET http://localhost:8080/api/readers/top5
Authorization: Basic librarian@library.com:Password1
```

### Get Overdue Lendings (LIBRARIAN role)
```http
GET http://localhost:8080/api/lendings/overdue
Authorization: Basic librarian@library.com:Password1
```

---

## 🔍 Troubleshooting

### Still Getting 401?

**Check:**
1. ✅ User exists in database (H2 console: `SELECT * FROM USER_ENTITY;`)
2. ✅ Password is correct (`Password1` for bootstrap users)
3. ✅ Username is exact (case-sensitive)
4. ✅ Authorization header is being sent (check in Postman)

**Debug:**
```bash
# Test login first
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"manuel@gmail.com","password":"Password1"}' \
  http://localhost:8080/api/public/login \
  -v
```

If login works, auth credentials are correct.

### Getting 403 Forbidden?

**Meaning:** Authenticated but wrong role

**Example:**
- Trying to POST `/api/books` (requires LIBRARIAN)
- But logged in as READER

**Solution:** Use a LIBRARIAN user or try endpoints that READERs can access.

---

## 📚 Documentation Files

I've created comprehensive guides:

1. **ENDPOINTS_GUIDE.md** - All API endpoints with examples
2. **AUTHENTICATION_GUIDE.md** - Complete auth documentation
3. **AUTHENTICATION_FIXES.md** - This file (quick fixes)

---

## ✅ Summary

| What | Status | URL/Info |
|------|--------|----------|
| H2 Console | ✅ Working | http://localhost:8080/h2-console |
| Swagger UI | ✅ Fixed | http://localhost:8080/swagger-ui/index.html |
| API Endpoints | ✅ Fixed | Use Basic Auth with `manuel@gmail.com:Password1` |
| Login Endpoint | ✅ Public | POST http://localhost:8080/api/public/login |

---

## 🎉 You're All Set!

The authentication is working as designed. The 401 errors were expected because the endpoints are protected. Now you know how to authenticate:

**For quick testing:** Use Basic Auth in Postman  
**For production:** Use JWT tokens from `/api/public/login`

Happy testing! 🚀

