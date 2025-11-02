# 🐛 ELASTICSEARCH AUTHOR ID FIX

**Date:** 2025-10-28  
**Issue:** NullPointerException when accessing `/api/books/search` with Elasticsearch

---

## 🔴 THE BUG

```
Cannot invoke "java.lang.Long.toString()" because the return value of 
"pt.psoft.g1.psoftg1.authormanagement.model.Author.getAuthorNumber()" is null
```

**Location:** `BookViewMapper.java:56` in `mapLinks()` method

**Root Cause:** When `BookDocumentMapper` created `Author` objects from Elasticsearch `BookDocument`:
```java
// OLD CODE - BROKEN
Author author = new Author(authorName, "Author bio not available in book index", null);
// ← authorNumber was NULL!
```

Then `BookViewMapper` tried to build author links:
```java
.path(author.getAuthorNumber().toString())  // ← BOOM! NullPointerException
```

---

## ✅ THE FIX

### 1. Added `authorIds` Field to `BookDocument`

**File:** `BookDocument.java`

```java
@Field(type = FieldType.Keyword)
private List<String> authors = new ArrayList<>();

@Field(type = FieldType.Long)
private List<Long> authorIds = new ArrayList<>();  // ← NEW!
```

**Why:** Store author IDs alongside author names so we can reconstruct the relationship

---

### 2. Updated `BookDocumentMapper.toModel()` to Use Author IDs

**File:** `BookDocumentMapper.java`

```java
for (int i = 0; i < document.getAuthors().size(); i++) {
    String authorName = document.getAuthors().get(i);
    Author author = new Author(authorName, "Author bio not available in book index", null);
    
    // ← NEW: Set authorNumber from stored IDs
    if (document.getAuthorIds() != null && i < document.getAuthorIds().size()) {
        author.setAuthorNumber(document.getAuthorIds().get(i));
    }
    
    authors.add(author);
}
```

**Why:** Reconstruct `Author` objects with both name AND ID

---

### 3. Updated `BookDocumentMapper.toDocument()` to Save Author IDs

**File:** `BookDocumentMapper.java`

```java
List<String> authorNames = new ArrayList<>();
List<Long> authorIds = new ArrayList<>();  // ← NEW

for (Author author : book.getAuthors()) {
    authorNames.add(author.getName().toString());
    if (author.getAuthorNumber() != null) {
        authorIds.add(author.getAuthorNumber());  // ← Store ID!
    }
}

document.setAuthors(authorNames);
document.setAuthorIds(authorIds);  // ← NEW!
```

**Why:** Persist author IDs so we don't lose them

---

### 4. Added Defensive Null Check in `BookViewMapper`

**File:** `BookViewMapper.java`

```java
List<Map<String, String>> authorLinks = book.getAuthors().stream()
    .filter(author -> author.getAuthorNumber() != null)  // ← NEW: Skip authors without IDs
    .map(author -> {
        String authorUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/authors/")
                .path(author.getAuthorNumber().toString())
                .toUriString();
        // ...
    })
    .collect(Collectors.toList());
```

**Why:** Prevent NullPointerException if an Author somehow doesn't have an ID

---

### 5. Added Defensive Null Check in `AuthorViewMapper`

**File:** `AuthorViewMapper.java`

```java
public Map<String, Object> mapLinks(final Author author){
    Map<String, Object> links = new HashMap<>();
    
    // Only generate links if author has an ID
    if (author.getId() != null) {
        String authorUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/authors/")
                .path(author.getId().toString())
                .toUriString();
        // ...
        links.put("author", authorUri);
        links.put("booksByAuthor", booksByAuthorUri);
    }
    
    links.put("photo", generatePhotoUrl(author));
    return links;
}
```

**Why:** Prevent crashes when rendering AuthorView without ID

---

## 🔄 ELASTICSEARCH INDEX NEEDS REFRESH

**CRITICAL:** The old Elasticsearch index does NOT have the `authorIds` field!

You MUST:
1. ✅ Stop the app
2. ✅ Delete the Elasticsearch container
3. ✅ Delete volumes (`docker volume prune -f`)
4. ✅ Start fresh Elasticsearch container
5. ✅ Restart app (bootstrapper will recreate data with authorIds)

**Use:** `restart-elasticsearch.ps1` script

---

## 📊 BEFORE vs AFTER

### ❌ BEFORE (Broken)

**BookDocument stored:**
```json
{
  "isbn": "9789723716160",
  "authors": ["Manuel Antonio Pina"]  // ← Only names, no IDs!
}
```

**Author object created:**
```java
author.getAuthorNumber() = null  // ← PROBLEM!
```

**BookViewMapper tried:**
```java
author.getAuthorNumber().toString()  // ← NullPointerException!
```

---

### ✅ AFTER (Fixed)

**BookDocument stored:**
```json
{
  "isbn": "9789723716160",
  "authors": ["Manuel Antonio Pina"],
  "authorIds": [1]  // ← NEW! IDs saved!
}
```

**Author object created:**
```java
author.getAuthorNumber() = 1L  // ← ID restored!
```

**BookViewMapper succeeds:**
```java
author.getAuthorNumber().toString() = "1"  // ← Success!
```

---

## 🧪 HOW TO TEST

1. Restart with fresh Elasticsearch (delete old data!)
2. Run test script: `test-elasticsearch-corrected.bat`
3. Test POST `/api/books/search`:

```bash
curl -X POST http://localhost:8080/api/books/search \
  -u maria@gmail.com:Mariaroberta!123 \
  -H "Content-Type: application/json" \
  -d '{"page":{"pageNumber":1,"pageSize":10},"query":{}}'
```

**Expected:** HTTP 200 with books containing author links:
```json
{
  "items": [
    {
      "title": "Como se Desenha Uma Casa",
      "authors": ["Manuel Antonio Pina"],
      "_links": {
        "authors": [
          { "href": "http://localhost:8080/api/authors/1" }
        ]
      }
    }
  ]
}
```

---

## 🎯 KEY LEARNINGS

1. **Elasticsearch stores FLAT data** - No nested Author objects, just names
2. **Domain models need IDs** - Without authorNumber, can't build links
3. **Mappers must preserve IDs** - When flattening to Elasticsearch, save IDs separately
4. **Always code defensively** - Check for null before calling `.toString()`

---

## 📝 FILES CHANGED

1. ✅ `BookDocument.java` - Added `authorIds` field
2. ✅ `BookDocumentMapper.java` - Store/retrieve author IDs
3. ✅ `BookViewMapper.java` - Null-safe author link generation
4. ✅ `AuthorViewMapper.java` - Null-safe link generation
5. ✅ `restart-elasticsearch.ps1` - Fresh restart script

---

## ⚠️ IMPORTANT NOTES

- **Index mapping changed** - Old Elasticsearch data incompatible!
- **MUST delete volumes** - `docker volume prune -f` before restart
- **Bootstrapper handles it** - Automatically populates authorIds on fresh start
- **SQL unaffected** - This fix only applies to Elasticsearch profile

---

**Status:** ✅ FIXED - Ready to test  
**Next:** Run `restart-elasticsearch.ps1` and verify endpoints work

