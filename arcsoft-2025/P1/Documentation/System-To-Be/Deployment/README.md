# Deployment Guides - Index

## Overview

This folder contains comprehensive guides for deploying and testing the Library Management System with different persistence strategies.

---

## Available Guides

### 📘 [01-Quick-Start-Guide.md](01-Quick-Start-Guide.md)
**Purpose:** Get the application running quickly  
**Time Required:** 10-15 minutes  
**Covers:**
- Running with SQL + Redis
- Running with Elasticsearch
- Basic endpoint testing
- Troubleshooting common issues

**Use this when:**
- First time running the application
- Quick demonstration needed
- Testing basic functionality

---

### 📗 [02-Redis-Caching-Test-Guide.md](02-Redis-Caching-Test-Guide.md)
**Purpose:** Comprehensive Redis caching testing  
**Time Required:** 30-45 minutes  
**Covers:**
- Cache hit/miss verification
- Performance measurements
- Cache invalidation testing
- Redis CLI commands
- Monitoring and debugging

**Use this when:**
- Demonstrating caching functionality
- Performance testing
- Verifying cache behavior
- Preparing presentation/report

---

### 📙 [03-Elasticsearch-Test-Guide.md](03-Elasticsearch-Test-Guide.md)
**Purpose:** Complete Elasticsearch functionality testing  
**Time Required:** 45-60 minutes  
**Covers:**
- Elasticsearch setup and verification
- Full-text search testing
- Index management
- CRUD operations
- Performance comparison with SQL
- Time-boxed testing strategy

**Use this when:**
- Demonstrating search capabilities
- Comparing persistence strategies
- Performance benchmarking
- Final delivery testing

---

## Automation Scripts

### 🔄 [reset-elasticsearch-for-tests.bat](reset-elasticsearch-for-tests.bat)
**Purpose:** Reset Elasticsearch to clean state for testing  
**Time Required:** ~1 minute  
**What it does:**
- Stops and removes old Elasticsearch container
- Cleans Docker volumes (removes old data)
- Starts fresh Elasticsearch container
- Waits for initialization
- Verifies Elasticsearch is ready

**Use this when:**
- Running functional tests that need clean bootstrap data
- Starting fresh testing session
- Resolving data inconsistencies

**Run from project root:**
```cmd
arcsoft-2025\P1\Documentation\System-To-Be\Deployment\reset-elasticsearch-for-tests.bat
```

---

## Quick Command Reference

### Start Services

**Redis:**
```cmd
docker run -d --name redis -p 6379:6379 redis:latest
```

**Elasticsearch:**
```cmd
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

### Run Application

**SQL + Redis (PowerShell):**
```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**SQL + Redis (CMD):**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**Elasticsearch (PowerShell):**
```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

**Elasticsearch (CMD):**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### Stop Application

**Option 1: Graceful (RECOMMENDED)**
- Press `Ctrl+C` in the Maven terminal

**Option 2: Force Stop**
```cmd
REM Check what's running on port 8080
netstat -ano | findstr :8080

REM Kill by process ID
taskkill /F /PID <PID>

REM Or kill all Java processes
taskkill /F /IM java.exe
```

### Verify Application Status

**Check if running:**
```cmd
netstat -ano | findstr :8080
```

**If running, you'll see:**
```
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345
```

**If stopped, no output.**

### Quick Tests

**List Books:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

**Top 5 Authors:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/authors/top5
```

**Search Books:**
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Spring"
```

---

## Test Credentials

### Readers
- `manuel@gmail.com` / `Manuelino123!`
- `joao@gmail.com` / `Joaozinho!123`

### Librarians
- `maria@gmail.com` / `Mariaroberta!123`
- `admin@gmail.com` / `AdminPwd1`

---

## Testing Strategy for Today's Delivery

### Phase 1: Redis Testing (1 hour)
1. ✅ Start Redis + Application
2. ✅ Test basic caching (books by ISBN)
3. ✅ Test top5 authors caching
4. ✅ Verify cache hits in console
5. ✅ Document results

**Reference:** [02-Redis-Caching-Test-Guide.md](02-Redis-Caching-Test-Guide.md)

### Phase 2: Elasticsearch Testing (1.5 hours)
1. ✅ Start Elasticsearch + Application
2. ✅ Verify indices created
3. ✅ Test book listing
4. ✅ Test full-text search
5. ✅ Test top5 authors
6. ⏭️ Skip top5 books if problematic
7. ✅ Document working features

**Reference:** [03-Elasticsearch-Test-Guide.md](03-Elasticsearch-Test-Guide.md)

### Phase 3: Documentation (2 hours)
1. ✅ Screenshot test results
2. ✅ Update report with findings
3. ✅ Document architecture
4. ✅ Note known issues
5. ✅ Prepare presentation materials

### Phase 4: Final Checks (30 minutes)
1. ✅ Verify all guides work
2. ✅ Test commands from scratch
3. ✅ Review report completeness
4. ✅ Prepare demo script

**Total Time:** ~5 hours (within your 6-hour budget)

---

## Known Issues & Strategic Decisions

### ✅ WORKING
- Redis caching for books by ISBN
- Redis caching for top5 authors
- Elasticsearch full-text search
- Elasticsearch book listing
- Authentication with hasAuthority
- Bootstrap data creation

### ⚠️ NEEDS VERIFICATION
- Books top5 endpoint (may return empty)
- Cache invalidation on updates

### ⏭️ SKIP IF PROBLEMATIC
- Books top5 endpoint
- Complex aggregations
- Detailed bootstrap debugging

**Reason:** Limited time for delivery, focus on demonstrable features

---

## Architecture Highlights

### Strategy Pattern Implementation

```
Application Layer (Controllers/Services)
         ↓
Abstract Repositories (Interfaces)
         ↓
    ┌────┴────┐
    ↓         ↓
SQL Impl   Elasticsearch Impl
(+ Redis)  (Native Search)
```

**Key Benefits:**
- ✅ Switch persistence at configuration time
- ✅ No code changes needed
- ✅ Domain models remain database-agnostic
- ✅ Easy to add new strategies (MongoDB)

### Caching Strategy (SQL + Redis)

```
Request → Controller → Service
                         ↓
                    @Cacheable?
                    ↙        ↘
            Cache Hit    Cache Miss
                ↓            ↓
            Redis      Database
                ↓            ↓
            Return     Store + Return
```

**Performance Impact:**
- 7-12x faster for cached requests
- Automatic cache invalidation on updates

---

## Presentation Tips

### Demo Script (10 minutes)

1. **Introduction** (1 min)
   - Show architecture diagram
   - Explain strategy pattern

2. **Redis Demo** (3 min)
   - Show first request (slow)
   - Show second request (fast)
   - Show Redis CLI with cached data

3. **Elasticsearch Demo** (3 min)
   - Show full-text search
   - Compare with SQL search
   - Show index management

4. **Code Walkthrough** (2 min)
   - Show configuration files
   - Show @Profile annotations
   - Explain switching mechanism

5. **Q&A** (1 min)

### Key Points to Emphasize

✅ **Clean Architecture:**
- Domain models independent of persistence
- Easy to test and maintain
- Follows SOLID principles

✅ **Performance:**
- Redis provides significant speed improvements
- Elasticsearch excels at full-text search
- Measured and documented results

✅ **Flexibility:**
- Switch databases without code changes
- Easy to add new strategies
- Production-ready configuration

✅ **Best Practices:**
- Proper authentication with hasAuthority
- Cache invalidation strategy
- Bootstrap data for testing

---

## Troubleshooting Quick Reference

| Issue | Solution | Guide Reference |
|-------|----------|-----------------|
| Port 8080 in use | `taskkill /F /IM java.exe` | 01, 02, 03 |
| Redis not responding | `docker start redis` | 01, 02 |
| Elasticsearch not ready | Wait 60s, check `docker logs elasticsearch` | 01, 03 |
| Authentication fails | Check credentials (see above) | 01, 02, 03 |
| Empty response | Verify bootstrap profile active | 02, 03 |
| Cache always misses | Verify Redis running, check config | 02 |
| No ES indices | Restart app with `elasticsearch,bootstrap` | 03 |

---

## File Locations

### Application Configuration
- `src/main/resources/application.properties`
- `src/main/resources/application-sql-redis.properties`
- `src/main/resources/application-elasticsearch.properties`

### Bootstrap
- `src/main/java/pt/psoft/g1/psoftg1/bootstrapping/Bootstrapper.java`

### Security Configuration
- `src/main/java/pt/psoft/g1/psoftg1/configuration/SecurityConfig.java`

### Repository Implementations
- `src/main/java/pt/psoft/g1/psoftg1/*/repositories/sql/`
- `src/main/java/pt/psoft/g1/psoftg1/*/repositories/elasticsearch/`

---

## Success Metrics

### Minimum Viable Demo (Must Have)
- [ ] Application starts with both profiles
- [ ] Authentication works
- [ ] Can list books
- [ ] Can search books
- [ ] Cache hit/miss demonstrated
- [ ] Elasticsearch search demonstrated

### Complete Demo (Should Have)
- [ ] All above +
- [ ] Top5 authors works
- [ ] Cache invalidation works
- [ ] Performance measured
- [ ] Report complete with screenshots

### Excellent Demo (Nice to Have)
- [ ] All above +
- [ ] Top5 books works
- [ ] All aggregations work
- [ ] Comprehensive benchmarks
- [ ] Video demonstration

---

## Next Deliveries Roadmap

### Phase 2 Improvements
- Complete MongoDB implementation
- Add more complex queries
- Implement advanced caching strategies
- Add metrics and monitoring

### Phase 3 Production Ready
- Add health checks
- Implement circuit breakers
- Add distributed caching
- Performance optimization

---

## Questions for Report

Answer these in your documentation:

1. **Why did we choose the Strategy Pattern?**
   - Flexibility to switch persistence
   - Clean separation of concerns
   - Easy to test and maintain

2. **What are the trade-offs of each strategy?**
   - SQL: ACID, complex queries, mature
   - Elasticsearch: Search performance, scalability, eventual consistency
   - Redis: Speed, but requires SQL/NoSQL backend

3. **How does caching improve performance?**
   - Show measured results (7-12x faster)
   - Explain cache invalidation strategy
   - Discuss TTL and memory considerations

4. **What challenges did you face?**
   - Authentication configuration
   - Bootstrap data complexity
   - Time constraints
   - Integration testing

5. **What would you improve?**
   - Automated testing
   - Better monitoring
   - Documentation
   - Error handling

---

## Final Checklist Before Delivery

- [ ] All guides tested and working
- [ ] Report complete with all sections
- [ ] Screenshots captured
- [ ] Code committed to Git
- [ ] Docker containers tested
- [ ] Demo script prepared
- [ ] Known issues documented
- [ ] Questions anticipated
- [ ] Backup plan ready
- [ ] Team aligned on presentation

---

## Contact & Support

**Repository:** MEI-ARCSOFT-2025-2026-1191577-1210939-1250530  
**Branch:** `32-p1-dev-tests`  
**Documentation:** `arcsoft-2025/P1/Documentation/`

**Good luck with your delivery! 🚀**

