# 🎯 QUICK REFERENCE - When to Use Which User

## 📚 For Books - 403 Error? Use This!

### ✅ GET /api/books - Use EITHER user
```bash
# Works with Manuel (READER)
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books

# Also works with Maria (LIBRARIAN)
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books
```

### ❌ GET /api/books/top5 - Use MARIA (LIBRARIAN)
```bash
# ❌ FAILS with Manuel - 403 Forbidden
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5

# ✅ WORKS with Maria
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

---

## 🔑 When You Get 403 Forbidden...

### If endpoint is `/api/books/top5` or any report
→ **Switch to LIBRARIAN (Maria)**

### If endpoint is `/api/genres/*` (anything)
→ **Switch to LIBRARIAN (Maria)**

### If endpoint is `/api/lendings/overdue`
→ **Switch to LIBRARIAN (Maria)**

### If you're trying to CREATE/UPDATE/DELETE anything
→ **Switch to LIBRARIAN (Maria)**

---

## 📋 Quick Decision Tree

```
Are you just VIEWING data?
├─ YES → Use Manuel (READER) ✅
│   └─ Examples: GET /api/books, GET /api/authors
│
└─ NO → Do you need REPORTS or MANAGEMENT?
    └─ YES → Use Maria (LIBRARIAN) ✅
        └─ Examples: top5, statistics, create/update
```

---

## 🚀 Most Common Working Commands

### For General Browsing (Use Manuel)
```bash
# List books
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books

# List authors
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors

# Search books
curl -u manuel@gmail.com:Manuelino123! -X POST \
  -H "Content-Type: application/json" \
  -d '{"title":"Harry"}' \
  http://localhost:8080/api/books/search
```

### For Reports & Management (Use Maria)
```bash
# Top 5 books
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5

# Overdue lendings
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/lendings/overdue

# Top 5 readers
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/readers/top5

# Genre statistics
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/genres/top5
```

---

## 🎭 User Credentials

### READER (for viewing)
```
Username: manuel@gmail.com
Password: Manuelino123!
Can: View books, authors, search, own profile
Cannot: Reports, create/update, genre stats
```

### LIBRARIAN (for everything)
```
Username: maria@gmail.com
Password: Mariaroberta!123
Can: EVERYTHING (all reader access + management + reports)
```

---

## 💡 Pro Tips

1. **Getting 403?** → You're probably using READER for a LIBRARIAN endpoint
2. **Use Maria for reports** → All `/top5`, statistics, and genre endpoints
3. **Use Manuel for testing** → General book/author viewing and search
4. **Check the HTTP method** → POST/PUT/PATCH/DELETE usually need LIBRARIAN

---

## 📖 Full Permissions Guide

For complete endpoint permissions, see:
- **ENDPOINT_PERMISSIONS.md** - Detailed table of all endpoints and roles

---

**Most Important:**
- Viewing data → Manuel ✅
- Reports/stats → Maria ✅
- Creating/updating → Maria ✅
- Your got 403? → Try Maria ✅

