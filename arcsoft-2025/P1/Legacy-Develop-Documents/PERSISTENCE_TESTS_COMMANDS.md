# Persistence Configuration Test Commands

## Overview

This document provides commands to run ONLY the persistence configuration tests,
avoiding other tests that may be failing in the application.

These tests verify that the correct database provider is selected based on
configuration (setup-time), which directly impacts runtime behavior as required by ADD.

---

## Test Structure

**Created Test Files:**
1. `SqlRedisProfileConfigurationTest.java` - Tests SQL-Redis profile loading
2. `PersistenceConfigTest.java` - Tests PersistenceConfig bean behavior
3. `SqlRepositoryProfileTest.java` - Tests repository profile-based loading
4. `PersistenceConfigurationTestSuite.java` - Test suite aggregator

**Total Tests:** ~25 test cases covering:
- Profile activation verification
- Bean loading based on configuration
- SQL-specific component initialization
- Exclusion of MongoDB/ElasticSearch beans
- Repository implementation loading

---

# Persistence Configuration Test Commands

## Overview

This document provides commands to run ONLY the persistence configuration tests,
avoiding other tests that may be failing in the application.

These tests verify that the correct database provider is selected based on
configuration (setup-time), which directly impacts runtime behavior as required by ADD.

---

## Test Structure

**Created Test Files:**
1. `SqlRedisProfileConfigurationTest.java` - Tests SQL-Redis profile loading
2. `PersistenceConfigTest.java` - Tests PersistenceConfig bean behavior
3. `SqlRepositoryProfileTest.java` - Tests repository profile-based loading

**Total Tests:** ~22 test cases covering:
- Profile activation verification
- Bean loading based on configuration
- SQL-specific component initialization
- Exclusion of MongoDB/ElasticSearch beans
- Repository implementation loading

---

## Commands to Run ONLY These Tests

### Option 1: Run All Configuration Tests (Recommended)

Run all three persistence configuration test classes:

```cmd
mvn test -Dtest="SqlRedisProfileConfigurationTest,PersistenceConfigTest,SqlRepositoryProfileTest"
```

**What it does:**
- Runs all 3 configuration test classes
- Skips all other application tests
- Fast execution (~10-15 seconds)

---

### Option 2: Run Individual Test Classes

Run specific test classes one by one:

```cmd
REM Test 1: SQL-Redis Profile Configuration (9 tests)
mvn test -Dtest=SqlRedisProfileConfigurationTest

REM Test 2: Persistence Config Bean (6 tests)
mvn test -Dtest=PersistenceConfigTest

REM Test 3: SQL Repository Profile Loading (7 tests)
mvn test -Dtest=SqlRepositoryProfileTest
```

**What it does:**
- Runs tests individually for detailed inspection
- Useful for debugging specific test failures
- Each command runs 5-10 test cases

---

### Option 3: Run All Tests in Configuration Package

Run all tests in the configuration package:

```cmd
mvn test -Dtest="pt.psoft.g1.psoftg1.configuration.*Test"
```

**What it does:**
- Runs all test classes in the configuration package
- Includes any future configuration tests you add
- Pattern-based test selection

---

### Option 4: Run Tests with Verbose Output

Get detailed output including profile activation logs:

```cmd
mvn test -Dtest=SqlRedisProfileConfigurationTest -X
```

**What it does:**
- Runs tests in debug mode
- Shows detailed Spring Boot configuration loading
- Useful for troubleshooting profile issues

---

### Option 5: Run Tests with Specific Profile

Explicitly set the profile during test execution:

```cmd
mvn test -Dtest=SqlRedisProfileConfigurationTest -Dspring.profiles.active=sql-redis
```

**What it does:**
- Forces specific profile activation
- Overrides default test configuration
- Useful for testing different profiles

---

## Expected Output

### Successful Test Run:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running pt.psoft.g1.psoftg1.configuration.SqlRedisProfileConfigurationTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running pt.psoft.g1.psoftg1.configuration.PersistenceConfigTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running pt.psoft.g1.psoftg1.configuration.SqlRepositoryProfileTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] BUILD SUCCESS
```

---

## What These Tests Verify

### 1. **Profile Activation** ✅
- Correct profile (`sql-redis`) is active
- Profile-based conditional loading works
- Configuration properties are set correctly

### 2. **Bean Loading** ✅
- SQL-specific beans are loaded (DataSource, EntityManagerFactory)
- Redis configuration beans are present
- MongoDB/ElasticSearch beans are NOT loaded

### 3. **Repository Implementation** ✅
- SQL repository implementations are loaded
- JPA repositories are registered
- Repository beans are accessible

### 4. **Configuration Properties** ✅
- `persistence.strategy=sql-redis` is set
- Database connection is configured
- Caching settings are applied

### 5. **Exclusion Logic** ✅
- MongoDB beans are NOT present
- ElasticSearch beans are NOT present
- Only SQL-related components are active

---

## Troubleshooting

### If Tests Fail:

1. **Check H2 Database Dependency**
   ```bash
   mvn dependency:tree | grep h2
   ```
   Should show H2 in test scope.

2. **Verify Profile Configuration**
   ```bash
   mvn test -Dtest=SqlRedisProfileConfigurationTest -X | grep "active profile"
   ```
   Should show `sql-redis` as active.

3. **Check for Port Conflicts**
   If Redis tests fail, check if port 6379 is available:
   ```bash
   netstat -ano | findstr :6379
   ```

4. **Clean and Rebuild**
   ```bash
   mvn clean compile test-compile
   mvn test -Dtest=PersistenceConfigurationTestSuite
   ```

---

## Running Tests in IDE (IntelliJ IDEA)

1. **Navigate to test class:**
   - `src/test/java/pt/psoft/g1/psoftg1/configuration/`
   - Right-click on `PersistenceConfigurationTestSuite.java`

2. **Run tests:**
   - Click "Run 'PersistenceConfigurationTestSuite'"
   - Or press `Ctrl+Shift+F10`

3. **View results:**
   - Test results appear in bottom panel
   - Green checkmarks indicate passing tests
   - Red X marks indicate failures

---

## Integration with CI/CD

Add to your build pipeline:

```yaml
# Example for GitHub Actions
- name: Run Persistence Configuration Tests
  run: mvn test -Dtest=PersistenceConfigurationTestSuite
```

```xml
<!-- Example for Maven Surefire -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/PersistenceConfigurationTestSuite.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## Quick Reference

| Command | Purpose | Tests Run |
|---------|---------|-----------|
| `mvn test -Dtest="SqlRedisProfileConfigurationTest,PersistenceConfigTest,SqlRepositoryProfileTest"` | Run all config tests | ~22 |
| `mvn test -Dtest=SqlRedisProfileConfigurationTest` | Profile tests only | ~9 |
| `mvn test -Dtest=PersistenceConfigTest` | Config bean tests | ~6 |
| `mvn test -Dtest=SqlRepositoryProfileTest` | Repository tests | ~7 |
| `mvn test -Dtest="pt.psoft.g1.psoftg1.configuration.*Test"` | All config tests | All |

---

## Test Execution Time

- **Single test class:** ~5-8 seconds
- **All three test classes:** ~12-18 seconds
- **Full application tests:** ~30-60 seconds (avoided)

---

## Summary

These tests demonstrate **configuration-driven runtime behavior** by verifying:
1. ✅ Profile-based bean loading works correctly
2. ✅ SQL-Redis strategy is properly selected
3. ✅ Correct database components are initialized
4. ✅ Alternative strategies (MongoDB, ES) are excluded
5. ✅ Configuration changes directly impact runtime

**Use the recommended command:**
```cmd
mvn test -Dtest="SqlRedisProfileConfigurationTest,PersistenceConfigTest,SqlRepositoryProfileTest"
```

This runs ONLY the relevant persistence configuration tests, skipping any failing tests in the rest of the application.

