# 🎭 API Endpoint Permissions Guide

## Quick Reference - Who Can Access What?

### 👤 READER Role (Manuel, João, Pedro, etc.)
**Can access basic viewing and personal data**

### 👔 LIBRARIAN Role (Maria)
**Can access everything + management & reports**

---

## 📚 BOOKS Endpoints

| Endpoint | Method | READER | LIBRARIAN | Notes |
|----------|--------|--------|-----------|-------|
| `/api/books` | GET | ✅ | ✅ | List all books |
| `/api/books/{isbn}` | GET | ✅ | ✅ | Get specific book |
| `/api/books/{isbn}/photo` | GET | ✅ | ✅ | Get book cover photo |
| `/api/books/suggestions` | GET | ✅ | ❌ | Book suggestions for reader |
| `/api/books/search` | POST | ✅ | ✅ | Search books |
| `/api/books/top5` | GET | ❌ | ✅ | Top 5 books (reports) |
| `/api/books/{isbn}/avgDuration` | GET | ❌ | ✅ | Average lending duration |
| `/api/books/{isbn}` | PUT | ❌ | ✅ | Create/update book |
| `/api/books/{isbn}` | PATCH | ❌ | ✅ | Partial update book |
| `/api/books/{isbn}/photo` | DELETE | ❌ | ✅ | Delete book photo |

---

## 👥 AUTHORS Endpoints

| Endpoint | Method | READER | LIBRARIAN | Notes |
|----------|--------|--------|-----------|-------|
| `/api/authors` | GET | ✅ | ✅ | List all authors |
| `/api/authors/{authorNumber}` | GET | ✅ | ✅ | Get specific author |
| `/api/authors/{authorNumber}/photo` | GET | ✅ | ✅ | Get author photo |
| `/api/authors/{authorNumber}/books` | GET | ✅ | ❌ | Books by author |
| `/api/authors/{authorNumber}/coauthors` | GET | ✅ | ❌ | Author's co-authors |
| `/api/authors/top5` | GET | ✅ | ❌ | Top 5 authors |
| `/api/authors` | POST | ❌ | ✅ | Create new author |
| `/api/authors/{authorNumber}` | PATCH | ❌ | ✅ | Update author |
| `/api/authors/{authorNumber}/photo` | DELETE | ❌ | ✅ | Delete author photo |

---

## 📖 READERS Endpoints

| Endpoint | Method | READER | LIBRARIAN | Notes |
|----------|--------|--------|-----------|-------|
| `/api/readers` | GET | ✅ | ✅ | List all readers |
| `/api/readers/{year}/{seq}` | GET | ❌ | ✅ | Get specific reader (LIBRARIAN only) |
| `/api/readers` | PATCH | ✅ | ❌ | Update own profile |
| `/api/readers/photo` | GET | ✅ | ❌ | Get own photo |
| `/api/readers/photo` | DELETE | ✅ | ❌ | Delete own photo |
| `/api/readers/{year}/{seq}/photo` | GET | ✅ | ✅ | Get reader photo |
| `/api/readers/{year}/{seq}/lendings` | GET | ✅ | ❌ | Get reader's lendings (own only) |
| `/api/readers/search` | POST | ❌ | ✅ | Search readers |
| `/api/readers/top5` | GET | ❌ | ✅ | Top 5 readers |
| `/api/readers/top5ByGenre` | GET | ❌ | ✅ | Top 5 readers by genre |

---

## 📗 LENDINGS Endpoints

| Endpoint | Method | READER | LIBRARIAN | Notes |
|----------|--------|--------|-----------|-------|
| `/api/lendings/{year}/{seq}` | GET | ✅ | ✅ | Get specific lending |
| `/api/lendings/{year}/{seq}` | PATCH | ✅ | ❌ | Return a book (reader only) |
| `/api/lendings` | POST | ❌ | ✅ | Create new lending |
| `/api/lendings/search` | POST | ❌ | ✅ | Search lendings |
| `/api/lendings/avgDuration` | GET | ❌ | ✅ | Average lending duration |
| `/api/lendings/overdue` | GET | ❌ | ✅ | List overdue lendings |
| `/api/lendings/averageMonthlyPerReader` | GET | ❌ | ✅ | Average monthly per reader |

---

## 🎨 GENRES Endpoints (All LIBRARIAN only)

| Endpoint | Method | READER | LIBRARIAN | Notes |
|----------|--------|--------|-----------|-------|
| `/api/genres/top5` | GET | ❌ | ✅ | Top 5 genres |
| `/api/genres/avgLendings` | GET | ❌ | ✅ | Average lendings |
| `/api/genres/avgLendingsPerGenre` | POST | ❌ | ✅ | Average per genre |
| `/api/genres/lendingsPerMonthLastTwelveMonths` | GET | ❌ | ✅ | Last 12 months stats |
| `/api/genres/lendingsAverageDurationPerMonth` | GET | ❌ | ✅ | Average duration per month |

---

## 🔓 PUBLIC Endpoints (No Authentication Required)

| Endpoint | Method | Authentication | Notes |
|----------|--------|----------------|-------|
| `/api/public/login` | POST | ❌ None | Login to get JWT token |
| `/api/readers` | POST | ❌ None | Register as new reader |
| `/h2-console/**` | GET | ❌ None | Database console |
| `/swagger-ui/**` | GET | ❌ None | API documentation |
| `/api-docs/**` | GET | ❌ None | OpenAPI specification |

---

## 🚀 Working Examples by Role

### As READER (Manuel)

#### ✅ These Work:
```bash
# Get all books
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books

# Get specific book
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/978-1-234-56789-0

# Search books
curl -u manuel@gmail.com:Manuelino123! -X POST \
  -H "Content-Type: application/json" \
  -d '{"title":"Sample"}' \
  http://localhost:8080/api/books/search

# Get all authors
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors

# Get book suggestions
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/suggestions

# Get own lendings (reader number 2025/1)
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/readers/2025/1/lendings

# Update own profile
curl -u manuel@gmail.com:Manuelino123! -X PATCH \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"999999999"}' \
  http://localhost:8080/api/readers
```

#### ❌ These Will Fail (403 Forbidden):
```bash
# Top 5 books - LIBRARIAN only
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5

# Create book - LIBRARIAN only
curl -u manuel@gmail.com:Manuelino123! -X PUT \
  -H "Content-Type: application/json" \
  -d '{"isbn":"123","title":"New Book"}' \
  http://localhost:8080/api/books/123

# Overdue lendings - LIBRARIAN only
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/lendings/overdue

# Top 5 readers - LIBRARIAN only
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/readers/top5

# Any genre endpoint - LIBRARIAN only
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/genres/top5
```

---

### As LIBRARIAN (Maria)

#### ✅ All These Work:
```bash
# Everything readers can do PLUS:

# Top 5 books
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

# Create/update books
curl -u maria@gmail.com:Mariaroberta!123 -X PUT \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-1-234-56789-0","title":"New Book","description":"A great book"}' \
  http://localhost:8080/api/books/978-1-234-56789-0

# Create authors
curl -u maria@gmail.com:Mariaroberta!123 -X POST \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","bio":"Famous writer"}' \
  http://localhost:8080/api/authors

# Get overdue lendings
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/lendings/overdue

# Top 5 readers
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/readers/top5

# Genre statistics
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/genres/top5

# Search readers
curl -u maria@gmail.com:Mariaroberta!123 -X POST \
  -H "Content-Type: application/json" \
  -d '{"name":"Manuel"}' \
  http://localhost:8080/api/readers/search

# Create new lending
curl -u maria@gmail.com:Mariaroberta!123 -X POST \
  -H "Content-Type: application/json" \
  -d '{"readerNumber":"2025/1","isbn":"978-1-234-56789-0"}' \
  http://localhost:8080/api/lendings
```

---

## 🎯 Quick Decision Guide

### "I want to view books/authors" → Use READER (Manuel)
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

### "I want to see reports/statistics" → Use LIBRARIAN (Maria)
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

### "I want to create/modify data" → Use LIBRARIAN (Maria)
```bash
curl -u maria@gmail.com:Mariaroberta!123 -X PUT http://localhost:8080/api/books/...
```

### "I want to manage my own profile" → Use READER (Manuel)
```bash
curl -u manuel@gmail.com:Manuelino123! -X PATCH http://localhost:8080/api/readers
```

---

## ⚠️ Common 403 Errors Explained

### Error: "Trying to access /api/books/top5 with Manuel"
**Problem:** Manuel is a READER, but `/api/books/top5` requires LIBRARIAN role  
**Solution:** Use Maria's credentials instead

### Error: "Trying to POST to /api/books with Manuel"
**Problem:** Creating books requires LIBRARIAN role  
**Solution:** Use Maria's credentials

### Error: "Trying to access /api/genres/* with Manuel"
**Problem:** ALL genre endpoints require LIBRARIAN role  
**Solution:** Use Maria's credentials

### Error: "Trying to access /api/lendings/overdue with Manuel"
**Problem:** This report requires LIBRARIAN role  
**Solution:** Use Maria's credentials

---

## 📋 Summary Table

| Task | Use This User |
|------|---------------|
| Browse books/authors | Manuel (READER) ✅ |
| Search books | Manuel (READER) ✅ |
| View book suggestions | Manuel (READER) ✅ |
| Manage own profile | Manuel (READER) ✅ |
| View own lendings | Manuel (READER) ✅ |
| **Create/modify books** | Maria (LIBRARIAN) ✅ |
| **Create/modify authors** | Maria (LIBRARIAN) ✅ |
| **View reports/statistics** | Maria (LIBRARIAN) ✅ |
| **Manage lendings** | Maria (LIBRARIAN) ✅ |
| **View all reader details** | Maria (LIBRARIAN) ✅ |
| **Access genre endpoints** | Maria (LIBRARIAN) ✅ |

---

**Pro Tip:** If you get 403 Forbidden, you're probably using a READER account for a LIBRARIAN-only endpoint. Switch to Maria's credentials!

---

**Quick Reference:**
- READER (Manuel): `manuel@gmail.com` / `Manuelino123!`
- LIBRARIAN (Maria): `maria@gmail.com` / `Mariaroberta!123`

