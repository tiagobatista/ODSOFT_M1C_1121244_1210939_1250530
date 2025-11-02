# System Testing Documentation

This folder contains system-level functional test documentation and results for the Library Management System.

---

## Test Categories

### 1. Automated System Tests (JUnit)
**Location:** `src/test/java/pt/psoft/g1/psoftg1/systest/`

These are automated functional tests that run with Maven:

```cmd
REM Run all tests
mvn test

REM Run only Elasticsearch system tests
mvn test -Dtest=ElasticsearchSystemTest

REM Run tests with specific profile
mvn test -Dspring.profiles.active=elasticsearch
```

**Test Classes:**
- `ElasticsearchSystemTest.java` - System-level tests for Elasticsearch profile (10 test cases)

**Test Type:** Functional opaque-box with SUT = system  
**Framework:** JUnit 5 + Spring Boot Test + MockMvc

---

### 2. Manual Test Documentation
**File:** `SYSTEM_TESTS_ELASTICSEARCH_RESULTS.md`

This document contains:
- Detailed test case descriptions
- Expected vs. Actual results
- Curl command examples
- Use case format ("As Reader, I want...")
- Test evidence with JSON responses

**Use for:** Report evidence, presentation material, manual verification

---

## Running Automated Tests

### Prerequisites
✅ Java 17+  
✅ Maven 3.6+  
✅ Docker Desktop running  
✅ **Clean Elasticsearch container** (see below)

### ⚠️ CRITICAL: Clean State for Each Test Run

Since tests rely on bootstrap data, you must start with a **clean Elasticsearch instance** for consistent results.

**Quick Reset:**
```cmd
REM Run the reset script (from project root)
arcsoft-2025\P1\Documentation\System-To-Be\Deployment\reset-elasticsearch-for-tests.bat
```

**Manual Reset:**
```cmd
docker stop elasticsearch && docker rm elasticsearch
docker volume prune -f
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
timeout /t 45
```

Then start the application with bootstrap:
```cmd
mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

Wait for "Started PsoftG1Application", then in a NEW terminal:
```cmd
mvn test -Dtest=ElasticsearchSystemTest
```

### Command Options

**1. Run All Tests**
```cmd
mvn clean test
```

**2. Run Specific Test Class**
```cmd
mvn test -Dtest=ElasticsearchSystemTest
```

**3. Run Single Test Method**
```cmd
mvn test -Dtest=ElasticsearchSystemTest#testReaderCanViewTop5Books
```

**4. Run Tests with Profile**
```cmd
mvn test -Dspring.profiles.active=elasticsearch -Dtest=ElasticsearchSystemTest
```

**5. Run Tests with Coverage**
```cmd
mvn clean test jacoco:report
```

---

## Test Structure

### System Test Package: `pt.psoft.g1.psoftg1.systest`

```
src/test/java/pt/psoft/g1/psoftg1/
└── systest/
    └── elasticsearch/
        └── ElasticsearchSystemTest.java (10 tests)
```

### Test Cases Covered

| Test ID | Description | Role | Status |
|---------|-------------|------|--------|
| UC-001 | View Top 5 Books | Reader | ✅ |
| UC-002 | View Top 5 Books | Librarian | ✅ |
| UC-003 | View Top 5 Authors | Reader | ✅ |
| UC-004 | View Top 5 Genres | Librarian | ✅ |
| UC-005 | Get Book by ISBN | Reader | ✅ |
| UC-006 | Get Author by ID | Reader | ✅ |
| UC-007 | Reject Unauthenticated Access | None | ✅ |
| UC-008 | Reject Unauthorized Access | Reader | ✅ |
| UC-009 | Invalid ISBN Returns 404 | Reader | ✅ |
| UC-010 | Invalid Author ID Returns 404 | Reader | ✅ |

---

## Test Output Examples

### Successful Test Run
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.psoft.g1.psoftg1.systest.elasticsearch.ElasticsearchSystemTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 3.245 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Test Report Location
After running tests:
- **Console Output:** Terminal
- **Surefire Reports:** `target/surefire-reports/`
- **HTML Report:** `target/surefire-reports/index.html`
- **JaCoCo Coverage:** `target/site/jacoco/index.html` (if run with coverage)

---

## Understanding Test Results

### Test Annotations Explained

**`@SpringBootTest`**  
- Loads full application context
- Tests entire system end-to-end
- Simulates production environment

**`@AutoConfigureMockMvc`**  
- Configures MockMvc for testing HTTP endpoints
- No need for actual HTTP server

**`@ActiveProfiles({"elasticsearch"})`**  
- Activates Elasticsearch profile
- Loads Elasticsearch-specific beans
- Uses Elasticsearch repositories

**`@WithMockUser`**  
- Simulates authenticated user
- Sets username and roles
- Tests authorization rules

---

## Adding New System Tests

### 1. Create Test Class
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"your-profile"})
class YourSystemTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(username = "user@test.com", roles = {"ROLE"})
    void testYourUseCase() throws Exception {
        mockMvc.perform(get("/api/endpoint"))
            .andExpect(status().isOk());
    }
}
```

### 2. Run the Test
```cmd
mvn test -Dtest=YourSystemTest
```

### 3. Document Results
Update `SYSTEM_TESTS_ELASTICSEARCH_RESULTS.md` with:
- Test case description
- Expected result
- Actual result
- Status (PASS/FAIL)

---

## Integration with CI/CD

These tests can be integrated into CI/CD pipelines:

**GitHub Actions Example:**
```yaml
- name: Run System Tests
  run: mvn test -Dtest=ElasticsearchSystemTest
```

**GitLab CI Example:**
```yaml
test:
  script:
    - mvn clean test
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
```

---

## Troubleshooting

### Issue: Tests fail with "Connection refused"
**Solution:** Ensure Elasticsearch is running:
```cmd
docker ps | findstr elasticsearch
```

### Issue: Tests fail with "Authentication error"
**Solution:** Bootstrap data may not be loaded. Check application logs.

### Issue: "No tests found"
**Solution:** Ensure test class name ends with `Test`:
- ✅ `ElasticsearchSystemTest.java`
- ❌ `ElasticsearchSystemTests.java`

---

## Best Practices

### ✅ DO:
- Use descriptive test names (`testReaderCanViewTop5Books`)
- Add `@DisplayName` for readable reports
- Test both success and failure scenarios
- Verify HTTP status codes
- Check response content type
- Validate JSON structure
- Test authorization rules

### ❌ DON'T:
- Hard-code test data that might change
- Skip authentication in system tests
- Test implementation details
- Make tests dependent on each other
- Ignore failing tests

---

## Related Documentation

- **Deployment Guides:** `../Deployment/`
- **API Documentation:** `../../../Docs/`
- **Source Code:** `../../../../src/`
- **Test Code:** `../../../../src/test/`

---

## Contact & Support

For questions about these tests:
- Review test code in `src/test/java/pt/psoft/g1/psoftg1/systest/`
- Check manual test documentation
- Refer to deployment guides for setup

---

**Last Updated:** 2025-11-02  
**Test Coverage:** 10 automated system tests  
**Status:** All tests passing ✅

