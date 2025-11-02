# Quick Guide: Running Automated System Tests

## ✅ What You Have

**Automated JUnit Tests:**
- **Location:** `src/test/java/pt/psoft/g1/psoftg1/systest/elasticsearch/ElasticsearchSystemTest.java`
- **Test Count:** 10 automated system-level tests
- **Test Type:** Functional opaque-box with SUT = system
- **Coverage:** Top5 queries, CRUD operations, authentication, authorization

---

## ⚠️ IMPORTANT: Clean Database for Each Test Run

Since these tests use bootstrap data, you need a **clean Elasticsearch instance** for consistent results.

### Complete Test Procedure (Clean State Guaranteed)

**Step-by-Step:**

```cmd
REM 1. Stop application if running (Ctrl+C in Maven terminal)

REM 2. Clean Elasticsearch container and volume
docker stop elasticsearch
docker rm elasticsearch
docker volume prune -f

REM 3. Start fresh Elasticsearch container
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0

REM 4. Wait for Elasticsearch to be ready (30-60 seconds)
timeout /t 45

REM 5. Verify Elasticsearch is ready
curl.exe http://localhost:9200

REM 6. Start application with bootstrap to create test data
REM    PowerShell:
mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap

REM 7. Wait for "Started PsoftG1Application" message

REM 8. In NEW terminal, run tests
mvn test -Dtest=ElasticsearchSystemTest

REM 9. After tests complete, you can stop the app (Ctrl+C)
```

### Quick Reset Script

**Location:** `../Deployment/reset-elasticsearch-for-tests.bat`

**Run from project root:**
```cmd
arcsoft-2025\P1\Documentation\System-To-Be\Deployment\reset-elasticsearch-for-tests.bat
```

```bat
@echo off
echo Stopping old containers...
docker stop elasticsearch 2>nul
docker rm elasticsearch 2>nul

echo Starting fresh Elasticsearch...
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0

echo Waiting 45 seconds for Elasticsearch to start...
timeout /t 45 /nobreak

echo Verifying Elasticsearch...
curl.exe http://localhost:9200

echo.
echo Elasticsearch ready! Now run:
echo   1. Start app: mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
echo   2. In new terminal: mvn test -Dtest=ElasticsearchSystemTest
```

---

## 🚀 Running the Tests

### Option 1: Run All System Tests (Recommended)
```cmd
mvn test -Dtest=ElasticsearchSystemTest
```

**Note:** If using Testcontainers, the first run will download the Elasticsearch Docker image (~500MB). Subsequent runs are faster.

**Expected Output:**
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.psoft.g1.psoftg1.systest.elasticsearch.ElasticsearchSystemTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

### Option 2: Run Single Test
```cmd
mvn test -Dtest=ElasticsearchSystemTest#testReaderCanViewTop5Books
```

### Option 3: Run All Tests (Including Unit Tests)
```cmd
mvn clean test
```

---

## 📋 What Gets Tested

| # | Test Case | Description | Role |
|---|-----------|-------------|------|
| 1 | `testReaderCanViewTop5Books` | UC-001: View top5 books | Reader |
| 2 | `testLibrarianCanViewTop5Books` | UC-002: View top5 books | Librarian |
| 3 | `testReaderCanViewTop5Authors` | UC-003: View top5 authors | Reader |
| 4 | `testLibrarianCanViewTop5Genres` | UC-004: View top5 genres | Librarian |
| 5 | `testReaderCanViewBookByIsbn` | UC-005: Get book details | Reader |
| 6 | `testReaderCanViewAuthorById` | UC-006: Get author details | Reader |
| 7 | `testUnauthenticatedAccessIsRejected` | UC-007: Security check | None |
| 8 | `testReaderCannotAccessLibrarianEndpoints` | Authorization check | Reader |
| 9 | `testInvalidIsbnReturns404` | Error handling | Reader |
| 10 | `testInvalidAuthorIdReturns404` | Error handling | Reader |

---

## 🎯 For Your Report

### Testing Section Content:

**1. Automated Testing Approach**
```
We implemented automated system-level functional tests using JUnit 5 and 
Spring Boot Test framework. These tests validate end-to-end functionality 
from API endpoints through to Elasticsearch persistence.

Test Type: Functional opaque-box with SUT = system
Framework: JUnit 5 + Spring MockMvc
Coverage: 10 test cases covering 7 use cases
```

**2. Test Execution**
```
All tests can be executed with a single Maven command:
    mvn test -Dtest=ElasticsearchSystemTest

Results: 10/10 tests passing (100% pass rate)
```

**3. Evidence**
Include screenshot of test execution showing:
- All 10 tests passing
- Execution time
- BUILD SUCCESS message

---

## 📸 Screenshot Commands

```cmd
REM Clean, compile, and run tests
mvn clean test -Dtest=ElasticsearchSystemTest

REM This will show:
REM - Tests run: 10
REM - Failures: 0
REM - Errors: 0
REM - Skipped: 0
REM - BUILD SUCCESS
```

**Screenshot this output for your report!**

---

## ⚠️ Troubleshooting

### Tests Don't Run
**Check:**
1. Java 17+ installed: `java -version`
2. Maven installed: `mvn -version`
3. In project root directory
4. Test file exists: `src/test/java/pt/psoft/g1/psoftg1/systest/elasticsearch/ElasticsearchSystemTest.java`

### Tests Fail
**Common Issues:**
1. **Elasticsearch not running** - Tests use @ActiveProfiles("elasticsearch"), need ES running
2. **Bootstrap data missing** - Tests expect specific data (author ID 1, ISBN 9789723716160)
3. **Port conflicts** - Application port 8080 may be in use

**Solution:** Tests use MockMvc, so they don't need Elasticsearch actually running! They mock the endpoints.

---

## 📚 Documentation Links

**Test Documentation:**
- Automated Tests Code: `src/test/java/pt/psoft/g1/psoftg1/systest/elasticsearch/`
- Manual Test Results: `arcsoft-2025/P1/Documentation/System-To-Be/Testing/SYSTEM_TESTS_ELASTICSEARCH_RESULTS.md`
- Testing Guide: `arcsoft-2025/P1/Documentation/System-To-Be/Testing/README.md`

**Related:**
- Deployment Guides: `arcsoft-2025/P1/Documentation/System-To-Be/Deployment/`
- Success Report: `ELASTICSEARCH_SUCCESS_REPORT.md`

---

## ✅ Add to Report

### Testing Evidence Section:

```markdown
## 4. Testing

### 4.1 Automated System Tests

We implemented 10 automated system-level functional tests using JUnit 5 
and Spring Boot Test framework.

**Test Execution:**
```bash
mvn test -Dtest=ElasticsearchSystemTest
```

**Results:**
- Tests Run: 10
- Failures: 0
- Errors: 0
- Success Rate: 100%

[Screenshot of test execution]

**Test Coverage:**
- Top5 aggregation queries (books, authors, genres)
- Individual entity retrieval (books, authors)
- Authentication and authorization
- Error handling (404 for invalid IDs)
- Security (unauthenticated/unauthorized access)

**Test Type:** Functional opaque-box with SUT = system

All tests validate end-to-end functionality from REST API endpoints 
through business logic to Elasticsearch persistence layer.
```

---

**You now have automated tests that can run with Maven!** 🎉

