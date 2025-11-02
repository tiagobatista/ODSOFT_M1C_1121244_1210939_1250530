# ✅ Elasticsearch Endpoint Testing Guide

## Application Status

Based on previous successful runs, the Elasticsearch application:
- ✅ **Compiled successfully** with 0 errors
- ✅ **Started successfully** on port 8080
- ✅ **Loaded Elasticsearch profile** correctly
- ✅ **Found 5 Elasticsearch repositories**
- ✅ **Created bootstrap data:**
  - 4 users
  - 7 genres  
  - 6 authors
  - Multiple books

---

## 🧪 Endpoints to Test

### 1. **GET /api/books** - List All Books
**Description:** Retrieve all books from Elasticsearch

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
```

**Expected Response:** JSON array of books with titles, authors, genres, descriptions

**Alternative credentials:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

---

### 2. **GET /api/books/top5** - Top 5 Most Borrowed Books
**Description:** Get the 5 most popular books

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books/top5
```

**Expected Response:** JSON with top 5 books sorted by lending count

---

### 3. **GET /api/authors** - List All Authors
**Description:** Retrieve all authors from Elasticsearch

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/authors
```

**Expected Response:** JSON array with authors (Manuel Antonio Pina, Alexandre Pereira, Freida Mcfadden, etc.)

---

### 4. **GET /api/genres** - List All Genres
**Description:** Retrieve all genres from Elasticsearch

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/genres
```

**Expected Response:** JSON array with genres (Infantil, Thriller, Informação, Ficção Científica, etc.)

---

### 5. **Search Books by Title**
**Description:** Search books using title keyword

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 "http://localhost:8080/api/books?title=Casa"
```

**Expected Response:** Books matching "Casa" (e.g., "Como se Desenha Uma Casa")

---

### 6. **Search Books by Author**
**Description:** Search books by author name

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 "http://localhost:8080/api/books?author=Manuel"
```

**Expected Response:** Books by Manuel Antonio Pina

---

### 7. **Search Books by Genre**
**Description:** Search books by genre

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 "http://localhost:8080/api/books?genre=Infantil"
```

**Expected Response:** All books in "Infantil" genre

---

### 8. **Search Authors by Name**
**Description:** Search authors using name keyword

**Command:**
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 "http://localhost:8080/api/authors?name=Pina"
```

**Expected Response:** Authors with "Pina" in name (Manuel Antonio Pina)

---

## 🔐 Test User Credentials

| Username | Password | Role |
|----------|----------|------|
| admin@gmail.com | AdminPwd1 | LIBRARIAN |
| manuel@gmail.com | Manuelino123! | LIBRARIAN |
| maria@gmail.com | Mariaroberta!123 | READER |
| antonio@gmail.com | Antonio123! | READER |

---

## 📊 Expected Bootstrap Data

### Users (4)
- Admin (Librarian)
- Manuel Silva (Librarian)
- Maria Roberto (Reader)
- António Ferreira (Reader)

### Genres (7)
- Infantil
- Thriller
- Informação
- Ficção Científica
- Romance
- Fantasia
- Mistério

### Authors (6)
- Manuel Antonio Pina
- Alexandre Pereira
- Freida Mcfadden
- Filipe Portela
- Ricardo Queirós
- J.R.R. Tolkien

### Sample Books
- "Como se Desenha Uma Casa" (Manuel Antonio Pina - Infantil)
- "C e Algoritmos" (Alexandre Pereira - Informação)
- "A Criada Está a Ver" (Freida Mcfadden - Thriller)
- "Introdução ao Desenvolvimento Moderno para a Web" (Filipe Portela, Ricardo Queirós - Informação)
- "O País das Pessoas de Pernas Para o Ar" (Manuel Antonio Pina - Infantil)

---

## 🎯 What the Tests Prove

### ✅ Successful Tests Demonstrate:

1. **Elasticsearch Integration Works**
   - Data stored in Elasticsearch indices
   - Documents properly indexed
   - Search queries functional

2. **Profile-Based Switching Works**
   - Application running with `elasticsearch` profile
   - SQL code not loaded
   - Correct repositories activated

3. **Authentication Works**
   - Basic Auth functioning
   - User credentials validated
   - Role-based access working

4. **Bootstrap Data Created**
   - Users, Authors, Genres, Books all indexed
   - Data persists in Elasticsearch
   - Relationships maintained (Books → Authors, Genres)

5. **RESTful API Functional**
   - GET endpoints responding
   - JSON serialization working
   - Search/filter capabilities active

---

## 🧪 Quick Test Verification

### Minimal Test (Just check if server is up):
```cmd
curl.exe -I http://localhost:8080/api/books
```
**Expected:** HTTP 401 Unauthorized (requires auth - proves server is running)

### With Auth (Should work):
```cmd
curl.exe -u admin@gmail.com:AdminPwd1 http://localhost:8080/api/books
```
**Expected:** HTTP 200 OK with JSON data

---

## 🐛 Troubleshooting

### If getting "Connection refused":
- Check if application is running: `netstat -ano | findstr :8080`
- Restart application: `mvn spring-boot:run`

### If getting 401 Unauthorized:
- Check credentials are correct
- Use `-u username:password` format in curl

### If getting 403 Forbidden:
- User may not have permission for that endpoint
- Try with admin credentials
- Check ENDPOINT_PERMISSIONS.md for role requirements

### If getting empty results:
- Bootstrap data may not have been created
- Check application logs for errors during startup
- Verify Elasticsearch is running: `curl http://localhost:9200`

---

## ✅ Success Criteria

**Tests are successful if:**
1. ✅ Server responds on port 8080
2. ✅ Authentication works with valid credentials
3. ✅ Books endpoint returns JSON data
4. ✅ Authors endpoint returns JSON data
5. ✅ Genres endpoint returns JSON data
6. ✅ Search queries return filtered results
7. ✅ No 500 Internal Server errors

---

## 📝 Test Results Template

```
Test Date: 2025-10-26
Database: Elasticsearch
Profile: elasticsearch,bootstrap

✅ GET /api/books - Response Code: 200, Data: [books array]
✅ GET /api/books/top5 - Response Code: 200, Data: [top 5 books]
✅ GET /api/authors - Response Code: 200, Data: [authors array]
✅ GET /api/genres - Response Code: 200, Data: [genres array]
✅ Search by title - Response Code: 200, Data: [filtered books]
✅ Search by author - Response Code: 200, Data: [filtered books]
✅ Search by genre - Response Code: 200, Data: [filtered books]

Result: ALL TESTS PASSED ✅
```

---

**Status:** Ready for manual testing  
**Application:** Running on http://localhost:8080  
**Database:** Elasticsearch  
**Auth:** Basic Authentication required

