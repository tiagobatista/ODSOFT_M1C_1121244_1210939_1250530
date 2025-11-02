# Authentication Guide - Quick Reference

## 🔐 Overview

The application uses **Spring Security** with:
- **JWT (JSON Web Tokens)** for stateless authentication
- **HTTP Basic Auth** as an alternative
- **Role-Based Access Control** (RBAC)

## 🎭 User Roles

### READER
- Can view books, authors, genres
- Can manage their own profile and photo
- Can view their own lendings
- **Cannot** create/modify books, authors, or other users

### LIBRARIAN
- Full access to manage books, authors, genres
- Can create/modify books and authors
- Can view all lendings and readers
- Can generate reports

### ADMIN
- Full access to everything
- Can manage all users

## 🚪 Public Endpoints (No Auth Required)

```
POST /api/public/login       - Login to get JWT token
POST /api/readers            - Register as a new reader
GET  /h2-console/**          - H2 Database Console
GET  /swagger-ui/**          - Swagger UI
GET  /api-docs/**            - API Documentation
```

## 🔑 Authentication Methods

### Method 1: Basic Auth (Recommended for Postman)

**Pros:** Simple, works immediately, no token management
**Cons:** Sends credentials with every request

**In Postman:**
1. Select **Authorization** tab
2. Type: **Basic Auth**
3. Username: `manuel@gmail.com`
4. Password: `Password1`
5. Make your request

**In cURL:**
```bash
curl -u manuel@gmail.com:Password1 http://localhost:8080/api/books
```

### Method 2: JWT Token

**Pros:** More secure, token-based, industry standard
**Cons:** Need to login first to get token

**Step 1 - Login:**
```http
POST http://localhost:8080/api/public/login
Content-Type: application/json

{
  "username": "manuel@gmail.com",
  "password": "Password1"
}
```

**Response:**
```http
HTTP/1.1 200 OK
Authorization: eyJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJleGFtcGxlLmlvIiwi...
Content-Type: application/json

{
  "id": 1,
  "username": "manuel@gmail.com",
  "fullName": "Manuel Sarapinto das Coives"
}
```

**Step 2 - Use the token:**

In Postman:
1. Copy the token from `Authorization` header
2. For next requests:
   - Authorization tab
   - Type: **Bearer Token**
   - Token: `<paste token here>`

In cURL:
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/books
```

## 👥 Default Users (Bootstrap)

If the `bootstrap` profile is active, these users are created automatically:

### Reader User
```
Username: manuel@gmail.com
Password: Password1
Role: READER
```

### Librarian User
```
Username: (check H2 console or bootstrap code)
Password: Password1
Role: LIBRARIAN
```

**To check available users:**
```sql
-- In H2 Console
SELECT id, username, enabled FROM USER_ENTITY;
```

## 📋 Postman Collection Setup

### Creating a Collection with Auth

1. **Create Collection**
   - Click "New" → "Collection"
   - Name it "Library API"

2. **Set Default Auth for Collection**
   - Click on collection name
   - Go to "Authorization" tab
   - Type: **Basic Auth**
   - Username: `manuel@gmail.com`
   - Password: `Password1`
   - **Save**

3. **All requests in collection inherit this auth!**

### Example Requests

#### Get All Books (READER role)
```
GET http://localhost:8080/api/books
Authorization: Inherited from parent (Basic Auth)
```

#### Create Book (LIBRARIAN role required)
```
POST http://localhost:8080/api/books
Authorization: Basic Auth
  Username: librarian@library.com
  Password: Password1
Content-Type: application/json

{
  "isbn": "978-1-234-56789-0",
  "title": "Sample Book",
  // ... more fields
}
```

## 🛡️ Security Configuration Summary

### Endpoint Permissions

#### Public (No Auth)
- `/api/public/**`
- `/h2-console/**`
- `/swagger-ui/**`

#### READER Only
- GET `/api/books`
- GET `/api/authors`
- GET `/api/readers/{year}/{seq}/lendings`
- PATCH `/api/readers` (own profile)

#### LIBRARIAN Only
- POST `/api/books`
- PATCH `/api/books/{isbn}`
- POST `/api/authors`
- GET `/api/lendings/overdue`
- All reporting endpoints

#### ADMIN Only
- Full access to everything

## 🔍 Testing Authentication

### Test 1: Public Endpoint (No Auth)
```bash
# Should work WITHOUT authentication
curl http://localhost:8080/api/public/login
```

### Test 2: Protected Endpoint (No Auth)
```bash
# Should return 401 Unauthorized
curl http://localhost:8080/api/books
```

### Test 3: Protected Endpoint (With Basic Auth)
```bash
# Should return 200 OK with books data
curl -u manuel@gmail.com:Password1 http://localhost:8080/api/books
```

### Test 4: Check Your Token
```bash
# Login and get token
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"username":"manuel@gmail.com","password":"Password1"}' \
  http://localhost:8080/api/public/login \
  -v

# Look for Authorization header in response
```

## 🚨 Common Authentication Errors

### 401 Unauthorized
**Meaning:** No authentication provided or invalid credentials

**Solutions:**
1. Check username/password are correct
2. Verify user exists in database (H2 console)
3. Make sure you're using Basic Auth or Bearer Token

### 403 Forbidden
**Meaning:** Authenticated but insufficient permissions

**Solutions:**
1. Check user role (READER vs LIBRARIAN)
2. Verify endpoint requires the role you have
3. Try with LIBRARIAN or ADMIN user

### 400 Bad Request on Login
**Meaning:** Invalid JSON or missing fields

**Solution:**
```json
{
  "username": "manuel@gmail.com",
  "password": "Password1"
}
```

## 📝 Swagger UI Authentication

Swagger UI has built-in authentication support:

1. Open: http://localhost:8080/swagger-ui/index.html
2. Click **Authorize** button (top right, lock icon)
3. Enter credentials:
   - Username: `manuel@gmail.com`
   - Password: `Password1`
4. Click **Authorize**
5. Click **Close**
6. Now all "Try it out" requests will include auth!

## 🔐 Security Best Practices

### For Development
- ✅ Use Basic Auth in Postman (simple)
- ✅ Use bootstrap users
- ✅ Check H2 console for user data

### For Production
- ❌ Don't use Basic Auth (sends password every time)
- ✅ Use JWT tokens
- ✅ Implement token refresh
- ✅ Use HTTPS only
- ✅ Short token expiry times (currently 1 hour)
- ✅ Store tokens securely

## 📚 Additional Resources

- **Spring Security Docs**: https://spring.io/projects/spring-security
- **JWT Introduction**: https://jwt.io/introduction
- **OAuth2 Resource Server**: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/

---

## Quick Reference Card

```
LOGIN:      POST /api/public/login
REGISTER:   POST /api/readers

BASIC AUTH: username:password encoded in Base64
JWT TOKEN:  Bearer <token> in Authorization header

DEFAULT USERS:
  Reader: manuel@gmail.com / Password1
  Librarian: (check H2 console)

ROLES: READER, LIBRARIAN, ADMIN
```

---

**Pro Tip:** For testing, always use Basic Auth in Postman - it's the easiest way to get started!

