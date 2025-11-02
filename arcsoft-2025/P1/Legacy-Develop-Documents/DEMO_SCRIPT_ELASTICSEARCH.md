# 🎬 ELASTICSEARCH DEMO SCRIPT - 5 Minutes

## Setup (Before Demo)
✅ Elasticsearch running on port 9200  
✅ Application running with `elasticsearch,bootstrap` profile  
✅ Terminal ready with commands  

---

## Demo Flow

### 1. INTRODUCTION (30 seconds)
**Say:**
"We implemented a flexible persistence architecture using the Strategy Pattern. Today I'll demonstrate our Elasticsearch implementation, which provides full-text search and advanced aggregation capabilities."

---

### 2. SHOW ELASTICSEARCH IS RUNNING (30 seconds)
**Command:**
```cmd
curl.exe http://localhost:9200
```

**Say:**
"First, let's verify Elasticsearch is operational..."

**Expected:** JSON response with cluster info

**Point out:**
- Cluster name
- Version (8.11.0)
- Tagline: "You Know, for Search"

---

### 3. SHOW DATA INDICES (1 minute)
**Command:**
```cmd
curl.exe http://localhost:9200/_cat/indices?v
```

**Say:**
"Our bootstrap process automatically created and populated these indices..."

**Point out:**
- `books` - 6 documents
- `authors` - 6 documents  
- `genres` - 7 documents
- `users` - 4 documents

**Explain:**
"This demonstrates automated index management and data initialization through our ElasticsearchBootstrapper."

---

### 4. DEMO TOP 5 BOOKS AGGREGATION (1 minute)
**Command:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books/top5
```

**Say:**
"Now let's see our most popular books using Elasticsearch aggregations..."

**Expected:** JSON with top 5 books

**Explain:**
"This query demonstrates Elasticsearch's aggregation capabilities, ranking books by lending count. Notice the authentication - we're using a READER account."

---

### 5. DEMO TOP 5 AUTHORS AGGREGATION (1 minute)
**Command:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

**Say:**
"We can also aggregate by authors..."

**Expected:**
```json
{
  "items": [
    {"authorName": "Manuel Antonio Pina", "lendingCount": 17},
    {"authorName": "Alexandre Pereira", "lendingCount": 15},
    {"authorName": "Freida Mcfadden", "lendingCount": 13},
    {"authorName": "Antoine de Saint Exupéry", "lendingCount": 12},
    {"authorName": "Filipe Portela", "lendingCount": 12}
  ]
}
```

**Point out:**
- Manuel Antonio Pina is most popular (17 lendings)
- Lending counts tracked automatically
- Complex aggregation query working seamlessly

---

### 6. SHOW RAW DATA IN ELASTICSEARCH (1 minute)
**Command:**
```cmd
curl.exe "http://localhost:9200/books/_search?size=2&pretty"
```

**Say:**
"Let's look at the actual data structure in Elasticsearch..."

**Point out in the response:**
- Full book details (title, ISBN, genre, description)
- Author information (names and IDs)
- Proper JSON structure
- Elasticsearch score (relevance ranking)

**Explain:**
"This shows how our domain models are automatically serialized and indexed for search."

---

### 7. DEMONSTRATE AUTHENTICATION (30 seconds)
**Command (will fail):**
```cmd
curl.exe -u wrong@user.com:WrongPwd! http://localhost:8080/api/books/top5
```

**Say:**
"Our security layer enforces proper authentication..."

**Expected:** 401 Unauthorized

**Then show success:**
```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

**Say:**
"With correct credentials, access is granted."

---

### 8. CLOSING - ARCHITECTURE BENEFITS (30 seconds)
**Say:**
"What makes this powerful is the Strategy Pattern implementation. We can switch between SQL+Redis, MongoDB, or Elasticsearch by changing a single configuration parameter - no code changes required."

**Show in editor (if time):**
- `@Profile("elasticsearch")` annotation
- Separate repository implementations
- Profile-based bean loading

**Conclude:**
"This demonstrates proper separation of concerns, dependency inversion, and configuration-driven architecture - all key principles of clean code and software architecture."

---

## BACKUP ANSWERS FOR QUESTIONS

### Q: "Why is the regular book list not working?"
**A:** "We identified a routing configuration issue with the pagination endpoint. The core Elasticsearch functionality - indexing, aggregations, and queries - is fully operational as demonstrated. This is documented as a known issue for future refinement."

### Q: "How does it compare to SQL?"
**A:** "Elasticsearch excels at full-text search and aggregations, as we've demonstrated. SQL provides stronger ACID guarantees and relational integrity. Our architecture allows us to choose the right tool for the right job - that's the power of the Strategy Pattern."

### Q: "Did you implement MongoDB too?"
**A:** "We configured the MongoDB infrastructure and repository interfaces, but focused implementation efforts on SQL+Redis and Elasticsearch for this delivery. The architecture is ready for MongoDB integration."

### Q: "What about Redis caching?"
**A:** "Redis caching is configured and operational in the SQL+Redis profile. Elasticsearch doesn't need external caching as it's inherently fast for search operations. We can demonstrate the Redis infrastructure if needed."

### Q: "What were the biggest challenges?"
**A:** "Managing different bootstrap strategies for different persistence implementations was complex. We learned that each strategy has unique initialization requirements. Separating concerns through profiles was the right architectural decision."

---

## TIMING BREAKDOWN

- Introduction: 0:00 - 0:30
- Elasticsearch status: 0:30 - 1:00  
- Show indices: 1:00 - 2:00
- Top5 books: 2:00 - 3:00
- Top5 authors: 3:00 - 4:00
- Raw data: 4:00 - 4:30
- Authentication: 4:30 - 5:00

**Total: 5 minutes**

---

## PRE-DEMO CHECKLIST

- [ ] Elasticsearch container running (`docker ps | findstr elasticsearch`)
- [ ] Application running with correct profile
- [ ] Test all commands work BEFORE demo
- [ ] Have terminal window sized appropriately
- [ ] Copy commands to notepad for easy paste
- [ ] Clear terminal history (`cls`) for clean demo
- [ ] Screenshot results as backup
- [ ] Know your answers to expected questions

---

## EMERGENCY BACKUP

**If something doesn't work during demo:**

1. **Show screenshots** (prepare beforehand!)
2. **Show Elasticsearch indices** (that always works)
3. **Explain architecture** from diagrams
4. **Show code** - @Profile annotations, strategy pattern
5. **Be honest:** "This worked in testing, let me show you the screenshots..."

**Remember:** Explaining WHY something should work and the architecture behind it is valuable even if live demo fails!

---

## POST-DEMO

**After successful demo, be ready to:**
- Show code structure
- Explain design decisions
- Discuss trade-offs (consistency vs. performance)
- Talk about what you learned
- Mention future improvements

---

**YOU'RE READY! Good luck! 🚀**

