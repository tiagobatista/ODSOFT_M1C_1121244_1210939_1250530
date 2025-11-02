# Test Coverage Analysis - Library Management System

**Date:** 2025-11-02  
**Project:** MEI-ARCSOFT-2025-2026 - Library Management System

---

## Required Tests (Based on Project Statement)

According to the project requirements, the following test types should be implemented:

1. ✅ **Functional opaque-box with SUT = system**
2. ❓ **Functional opaque-box with SUT = classes**
3. ❓ **Functional transparent-box with SUT = domain classes**
4. ❌ **Mutation tests with SUT = classes**
5. ❓ **Functional opaque-box with SUT = controller+service+{domain, repository, gateways}**

---

## Current Test Implementation Status

### 1. ✅ Functional Opaque-Box with SUT = System (IMPLEMENTED)

**Status:** ✅ **FULLY IMPLEMENTED**

**Location:** `src/test/java/pt/psoft/g1/psoftg1/systest/`

**Test Files:**
- `ElasticsearchSystemTest.java` - 10 test cases

**Characteristics:**
- End-to-end testing from API to persistence layer
- Uses `@SpringBootTest` with full application context
- Tests complete use cases (e.g., "As a Reader, I want to view Top 5 books")
- Uses MockMvc for HTTP endpoint testing
- Tests authentication and authorization
- Validates JSON responses and HATEOAS compliance

**Evidence:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"elasticsearch"})
class ElasticsearchSystemTest {
    @Test
    @WithMockUser(username = "manuel@gmail.com", authorities = {"READER"})
    void testReaderCanViewTop5Books() throws Exception {
        mockMvc.perform(get("/api/books/top5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
}
```

**Documentation:** `arcsoft-2025/P1/Documentation/System-To-Be/Testing/README.md`

---

### 2. ✅ Functional Opaque-Box with SUT = Classes (PARTIALLY IMPLEMENTED)

**Status:** ✅ **PARTIALLY IMPLEMENTED** - Domain model classes

**Location:** `src/test/java/pt/psoft/g1/psoftg1/*/model/`

**Test Files (Domain Classes):**
- `IsbnTest.java` - Tests ISBN value object
- `TitleTest.java` - Tests Title value object
- `DescriptionTest.java` - Tests Description value object
- `BookTest.java` - Tests Book entity
- `AuthorTest.java` - Tests Author entity
- `BioTest.java` - Tests Bio value object
- `GenreTest.java` - Tests Genre entity
- `LendingTest.java` - Tests Lending entity
- `LendingNumberTest.java` - Tests LendingNumber value object
- `ReaderTest.java` - Tests Reader entity
- `PhoneNumberTest.java` - Tests PhoneNumber value object
- `BirthDateTest.java` - Tests BirthDate value object
- `NameTest.java` - Tests Name value object
- `PhotoTest.java` - Tests Photo value object

**Characteristics:**
- Unit tests for individual domain classes
- Tests validation rules (null checks, format validation, business rules)
- Tests value object immutability
- Tests entity behavior
- Black-box approach (testing public interface only)

**Evidence:**
```java
class IsbnTest {
    @Test
    void ensureIsbnMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Isbn(null));
    }
    
    @Test
    void ensureChecksum13IsCorrect() {
        assertThrows(IllegalArgumentException.class, () -> new Isbn("9782826012099"));
    }
}
```

**Gap:** While domain model classes are well-tested, there may be other classes (utilities, helpers, DTOs) that need opaque-box testing.

---

### 3. ⚠️ Functional Transparent-Box with SUT = Domain Classes (NEEDS ASSESSMENT)

**Status:** ⚠️ **NEEDS DETAILED ASSESSMENT**

**Current Situation:**
The existing domain model tests appear to be primarily **opaque-box** (testing public interface), not **transparent-box** (white-box testing with knowledge of internal structure).

**What's Missing for Transparent-Box:**
- Path coverage testing
- Branch coverage testing
- Condition coverage testing
- Tests designed based on internal logic/implementation
- Tests for all execution paths in complex methods

**Example of Transparent-Box Test (NOT currently present):**
```java
// Transparent-box test would test internal branching logic
@Test
void testIsbnChecksumCalculation_Path1_EvenPosition() {
    // Test specific path in checksum calculation for even positions
}

@Test
void testIsbnChecksumCalculation_Path2_OddPosition() {
    // Test specific path in checksum calculation for odd positions
}
```

**Recommendation:** Add transparent-box tests that:
1. Analyze code coverage reports
2. Design tests to cover all branches and paths
3. Test edge cases based on internal implementation
4. Ensure 100% line and branch coverage for domain classes

---

### 4. ❌ Mutation Tests with SUT = Classes (NOT IMPLEMENTED)

**Status:** ❌ **NOT IMPLEMENTED**

**Evidence:**
- No PIT (Pitest) Maven plugin configuration found in `pom.xml`
- No mutation testing reports in project
- No mention of mutation testing in test documentation

**What's Missing:**
1. **PIT Maven Plugin Configuration:**
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.15.0</version>
    <configuration>
        <targetClasses>
            <param>pt.psoft.g1.psoftg1.*</param>
        </targetClasses>
        <targetTests>
            <param>pt.psoft.g1.psoftg1.*</param>
        </targetTests>
    </configuration>
</plugin>
```

2. **Mutation Testing Execution:**
```cmd
mvn org.pitest:pitest-maven:mutationCoverage
```

3. **Analysis of mutation coverage reports** to ensure test quality

**Recommendation:** 
- Add PIT plugin to pom.xml
- Run mutation tests on domain classes
- Aim for >80% mutation coverage
- Document results in report

---

### 5. ✅ Functional Opaque-Box with SUT = Controller+Service+{Domain, Repository, Gateways} (PARTIALLY IMPLEMENTED)

**Status:** ✅ **PARTIALLY IMPLEMENTED** - Integration tests exist

**Location:** `src/test/java/pt/psoft/g1/psoftg1/*/`

**Test Files:**
- `LendingServiceImplTest.java` - Integration test for service layer
- `AuthorServiceImplIntegrationTest.java` - Service layer integration test
- `AuthorRepositoryIntegrationTest.java` - Repository layer integration test
- `LendingRepositoryIntegrationTest.java` - Repository layer integration test
- `AuthorControllerIntegrationTest.java` - Controller layer test (appears minimal/stub)

**Characteristics:**
- Uses `@SpringBootTest` or `@WebMvcTest`
- Tests multiple layers together
- Uses real or embedded infrastructure (H2, embedded Redis)
- Tests service → repository → database flow
- Tests controller → service integration

**Evidence:**
```java
@Transactional
@SpringBootTest
class LendingServiceImplTest {
    @Autowired
    private LendingService lendingService;
    @Autowired
    private LendingRepository lendingRepository;
    @Autowired
    private BookRepository bookRepository;
    
    @Test
    void testServiceWithRepositories() {
        // Tests integration between service and repositories
    }
}
```

**Gap Analysis:**
- ✅ Service + Repository tests exist
- ✅ Some controller tests exist (but minimal)
- ❓ Gateway/external API integration tests may be missing
- ❓ Not all controllers have comprehensive integration tests

**Recommendation:** Expand integration tests to cover:
- All controllers with comprehensive test cases
- External API gateways (Google Books, Open Library, ISBNdb)
- Error handling across layers
- Transaction management

---

## Summary Table

| Test Type | SUT Level | Status | Coverage | Priority |
|-----------|-----------|--------|----------|----------|
| Functional Opaque-Box | System | ✅ Complete | 10 tests | HIGH |
| Functional Opaque-Box | Classes (Domain) | ✅ Partial | 14 tests | MEDIUM |
| Functional Transparent-Box | Domain Classes | ⚠️ Missing | 0% | HIGH |
| Mutation Tests | Classes | ❌ Missing | 0% | HIGH |
| Functional Opaque-Box | Controller+Service+... | ✅ Partial | 5 tests | MEDIUM |
| Configuration Tests | Config | ✅ Complete | 9 tests | LOW |

---

## Recommendations for Completion

### Priority 1: HIGH - Mutation Testing
**Action Items:**
1. Add PIT Maven plugin to `pom.xml`
2. Configure mutation testing for domain model packages
3. Run mutation tests: `mvn org.pitest:pitest-maven:mutationCoverage`
4. Analyze mutation coverage report
5. Improve tests to kill surviving mutants
6. Target: >80% mutation score

**Estimated Effort:** 4-8 hours

---

### Priority 2: HIGH - Transparent-Box Tests for Domain Classes
**Action Items:**
1. Generate code coverage report: `mvn clean test jacoco:report`
2. Analyze branch coverage for each domain class
3. Create transparent-box tests for uncovered branches
4. Document internal logic being tested
5. Target: 100% branch coverage

**Estimated Effort:** 6-12 hours

---

### Priority 3: MEDIUM - Expand Opaque-Box Class Tests
**Action Items:**
1. Identify all non-domain classes (DTOs, Utilities, Mappers, etc.)
2. Create unit tests for each class
3. Test public interfaces without knowledge of implementation
4. Focus on edge cases and error conditions

**Estimated Effort:** 4-6 hours

---

### Priority 4: MEDIUM - Complete Integration Tests
**Action Items:**
1. Expand controller integration tests (BookController, ReaderController, etc.)
2. Add gateway integration tests (external APIs)
3. Test cross-cutting concerns (caching, transactions)
4. Test error propagation across layers

**Estimated Effort:** 8-12 hours

---

## Test Execution Commands

### Run All Tests
```cmd
mvn clean test
```

### Run Specific Test Categories
```cmd
# Domain model tests
mvn test -Dtest="*Test" -DfailIfNoTests=false

# Integration tests
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false

# System tests
mvn test -Dtest="ElasticsearchSystemTest"

# Configuration tests
mvn test -Dtest="*Configuration*Test"
```

### Generate Coverage Reports
```cmd
# JaCoCo coverage
mvn clean test jacoco:report

# View report at: target/site/jacoco/index.html
```

### Run Mutation Tests (after adding PIT plugin)
```cmd
mvn org.pitest:pitest-maven:mutationCoverage

# View report at: target/pit-reports/index.html
```

---

## Test Statistics (Current State)

```
Total Test Files: 23
├── System Tests: 1 file (10 test cases)
├── Domain Model Tests: 14 files (~50+ test cases)
├── Service Integration Tests: 2 files
├── Repository Integration Tests: 2 files
├── Controller Tests: 1 file (minimal)
└── Configuration Tests: 3 files (22 test cases)

Estimated Total Test Cases: ~90+
```

---

## Conclusion

### ✅ What's Working Well:
1. **System-level tests** are comprehensive and well-documented
2. **Domain model unit tests** cover value objects and entities
3. **Configuration tests** ensure profile isolation
4. **Some integration tests** exist for critical flows

### ⚠️ What Needs Attention:
1. **Mutation testing** is completely missing - HIGH PRIORITY
2. **Transparent-box tests** are not explicitly designed
3. **Integration test coverage** could be expanded
4. **Controller tests** are minimal

### 📋 Next Steps:
1. Implement mutation testing with PIT
2. Design and implement transparent-box tests for domain classes
3. Expand integration test coverage
4. Document all test types in project report

---

**Note:** This analysis is based on the current state of the repository as of 2025-11-02. The test requirements from the project statement image indicate these five types of tests should be implemented for project evaluation.

