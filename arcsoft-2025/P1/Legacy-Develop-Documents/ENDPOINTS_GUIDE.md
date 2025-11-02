# Application Endpoints - Quick Reference Guide

## 🌐 Base URL
```
http://localhost:8080
```

## 📚 Documentation & Admin Endpoints

### Swagger UI (Interactive API Documentation)
```
http://localhost:8080/swagger-ui/index.html
```
**Best starting point!** This gives you a complete interactive interface to explore and test all API endpoints.

**Note:** The path is `/swagger-ui/index.html` (not `/swagger-ui.html`)

### API Documentation (OpenAPI/JSON)
```
http://localhost:8080/api-docs
```

### H2 Database Console
```
http://localhost:8080/h2-console
```
**Connection details:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave empty)

---

## 📖 Main API Endpoints

All API endpoints are prefixed with `/api`

### 1. Books (`/api/books`)

#### Get All Books
```
GET http://localhost:8080/api/books
```

#### Get Specific Book by ISBN
```
GET http://localhost:8080/api/books/978-1-234-56789-0
```
Replace `978-1-234-56789-0` with an actual ISBN from your database.

#### Get Book Photo
```
GET http://localhost:8080/api/books/978-1-234-56789-0/photo
```

#### Get Top 5 Books
```
GET http://localhost:8080/api/books/top5
```

---

### 2. Authors (`/api/authors`)

#### Get All Authors
```
GET http://localhost:8080/api/authors
```

#### Search Authors
```
GET http://localhost:8080/api/authors?name=John
```

---

### 3. Readers (`/api/readers`)

#### Get All Readers
```
GET http://localhost:8080/api/readers
```

#### Get Specific Reader
```
GET http://localhost:8080/api/readers/2024/1
```
Format: `/{year}/{sequenceNumber}`

#### Search Reader by Phone
```
GET http://localhost:8080/api/readers?phoneNumber=912345678
```

#### Search Reader by Name
```
GET http://localhost:8080/api/readers?name=Manuel
```

#### Get Reader Photo
```
GET http://localhost:8080/api/readers/2024/1/photo
```

#### Get Reader's Lendings
```
GET http://localhost:8080/api/readers/2024/1/lendings
```

#### Get Top 5 Readers
```
GET http://localhost:8080/api/readers/top5
```

#### Get Top 5 Readers by Genre
```
GET http://localhost:8080/api/readers/top5ByGenre?genre=Fiction
```

---

### 4. Lendings (`/api/lendings`)

#### Get Specific Lending
```
GET http://localhost:8080/api/lendings/2024/1
```
Format: `/{year}/{sequenceNumber}`

#### Get Average Duration
```
GET http://localhost:8080/api/lendings/avgDuration
```

#### Get Overdue Lendings
```
GET http://localhost:8080/api/lendings/overdue
```

#### Get Average Monthly Per Reader
```
GET http://localhost:8080/api/lendings/averageMonthlyPerReader
```

---

### 5. Genres (`/api/genres`)

#### Get Top 5 Genres
```
GET http://localhost:8080/api/genres/top5
```

#### Get Lendings Per Month (Last 12 Months)
```
GET http://localhost:8080/api/genres/lendingsPerMonthLastTwelveMonths
```

#### Get Average Duration Per Month
```
GET http://localhost:8080/api/genres/lendingsAverageDurationPerMonth
```

---

## 🔐 Authentication - IMPORTANT!

**All API endpoints require authentication!** That's why you're getting 401 errors in Postman.

The application uses **JWT (JSON Web Tokens)** for authentication with **HTTP Basic Auth** support.

### Login Endpoint
```
POST http://localhost:8080/api/public/login
```

**Request Body:**
```json
{
  "username": "manuel@gmail.com",
  "password": "Password1"
}
```

**Response:**
The JWT token is returned in the `Authorization` header and the user details in the body.

### How to Use in Postman

#### Option 1: Using Basic Auth (Simplest)
1. In Postman, go to the **Authorization** tab
2. Select **Type**: `Basic Auth`
3. Enter:
   - **Username**: `manuel@gmail.com`
   - **Password**: `Password1`
4. Make your request - Postman will handle the rest!

#### Option 2: Using JWT Token
1. First, login using the endpoint above
2. Copy the token from the `Authorization` header in the response
3. For subsequent requests:
   - Go to **Authorization** tab
   - Select **Type**: `Bearer Token`
   - Paste the token in the **Token** field

### Bootstrap Users (Available by Default)

If bootstrap is enabled, these users are created automatically:

**Librarian:**
- Username: `librarian@library.com` (or similar)
- Password: `Password1`
- Role: LIBRARIAN

**Reader:**
- Username: `manuel@gmail.com`
- Password: `Password1`
- Role: READER

**Note:** Check the bootstrap code or H2 console to see exact usernames.

### Checking Available Users

Connect to H2 Console and run:
```sql
SELECT * FROM USER_ENTITY;
```

This will show all available users and their usernames.

---

## 🚀 Quick Start Examples

### 1. **Check Database First** ✅
Open in your browser:
```
http://localhost:8080/h2-console
```
Then connect with:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

Run this SQL to see available users:
```sql
SELECT username, enabled FROM USER_ENTITY;
```

### 2. **Open Swagger UI** ✅
```
http://localhost:8080/swagger-ui/index.html
```
This shows ALL available endpoints with interactive testing.

**IMPORTANT:** You'll need to authenticate in Swagger:
1. Click the **Authorize** button (top right)
2. Enter username/password (e.g., `manuel@gmail.com` / `Password1`)
3. Click **Authorize**
4. Now you can test endpoints!

### 3. **Login via Postman** ✅
```
POST http://localhost:8080/api/public/login
```

**Body (raw JSON):**
```json
{
  "username": "manuel@gmail.com",
  "password": "Password1"
}
```

Then use **Basic Auth** in Postman for other requests.

### 4. **Test an API Endpoint with Auth** ✅
In Postman:
- URL: `http://localhost:8080/api/books`
- Authorization: Basic Auth
  - Username: `manuel@gmail.com`
  - Password: `Password1`

---

## 📝 Example Workflow

1. **Start Application**
   ```bash
   mvn spring-boot:run
   ```

2. **Open Swagger UI** (in browser)
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Explore Available Endpoints**
   - Click on any endpoint group (Books, Authors, Readers, etc.)
   - Click "Try it out" to test endpoints
   - See request/response examples

4. **Check Database** (in browser)
   ```
   http://localhost:8080/h2-console
   ```
   - View tables: `SELECT * FROM BOOK_ENTITY;`
   - View readers: `SELECT * FROM READER_ENTITY;`
   - View lendings: `SELECT * FROM LENDING_ENTITY;`

---

## 🔍 Common Use Cases

### Checking if App is Running
```
http://localhost:8080/swagger-ui.html
```
If this loads, the app is running!

### Testing Books API
```
http://localhost:8080/api/books
```

### Testing Readers API
```
http://localhost:8080/api/readers
```

### Viewing Bootstrap Data
```
http://localhost:8080/h2-console
```
Then run SQL queries to see what data was created.

---

## ⚠️ Troubleshooting

### "401 Unauthorized" Error ⚠️
**This is the most common issue!**

**Cause:** All API endpoints require authentication (except `/api/public/**`)

**Solutions:**
1. **In Postman**: Use Basic Auth
   - Authorization Tab → Type: Basic Auth
   - Username: `manuel@gmail.com`
   - Password: `Password1`

2. **Get a JWT Token**:
   - POST to `http://localhost:8080/api/public/login`
   - Use the token from the response header

3. **Check users exist**:
   - Go to H2 Console
   - Run: `SELECT * FROM USER_ENTITY;`

### "404 Not Found" Error
- Make sure the application is running
- Check the URL is correct
- **Swagger**: Use `/swagger-ui/index.html` (not `/swagger-ui.html`)

### "403 Forbidden" Error
- You're authenticated but don't have permission
- Check user role (READER vs LIBRARIAN)
- Some endpoints require specific roles

### "Connection Refused"
- Application is not running
- Start it with: `mvn spring-boot:run`
- Check it started successfully (look for "Started PsoftG1Application")

---

## 📌 Bookmarks to Save

Essential URLs to bookmark:

1. **Swagger UI**: http://localhost:8080/swagger-ui/index.html
2. **H2 Console**: http://localhost:8080/h2-console
3. **Login**: POST http://localhost:8080/api/public/login
4. **Books API**: http://localhost:8080/api/books (requires auth)
5. **Readers API**: http://localhost:8080/api/readers (requires auth)

---

## 💡 Pro Tips

1. **Always start with Swagger UI** - it shows all available endpoints with examples
2. **Use H2 Console** to see what data exists in the database
3. **Check the terminal logs** for any errors or issues
4. **Bootstrap data** is loaded automatically on startup (if configured)

---

**Happy Testing!** 🚀

The application is now running and ready to use at http://localhost:8080

