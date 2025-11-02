# 🎯 Persistence Tests - Quick Reference Card

## ⚡ Quick Commands

### Run All Configuration Tests
```cmd
mvn test -Dtest="SqlRedisProfileConfigurationTest,PersistenceConfigTest,SqlRepositoryProfileTest"
```

### Run Single Test Class
```cmd
mvn test -Dtest=SqlRedisProfileConfigurationTest
```

---

## 📊 Test Results

✅ **22 tests** - All passing  
⏱️ **~7 seconds** per test class  
🎯 **100% success rate**

---

## 📁 Test Files

1. `SqlRedisProfileConfigurationTest.java` - 9 tests
2. `PersistenceConfigTest.java` - 6 tests
3. `SqlRepositoryProfileTest.java` - 7 tests

**Location:** `src/test/java/pt/psoft/g1/psoftg1/configuration/`

---

## ✅ What's Tested

- ✅ Profile activation (`sql-redis`)
- ✅ SQL bean loading (DataSource, EntityManagerFactory)
- ✅ Redis configuration presence
- ✅ MongoDB/ES beans exclusion
- ✅ Repository implementations loading
- ✅ Configuration properties validation

---

## 📖 Documentation

- **PERSISTENCE_TESTS_SUMMARY.md** - Complete test documentation
- **PERSISTENCE_TESTS_COMMANDS.md** - All available commands
- **Test files** - Full test source code with comments

---

## 🎓 ADD Requirement Met

**Requirement:**  
"Alternatives must be defined during configuration (setup time), which directly impacts runtime behavior"

**Proof:**  
Tests verify that `persistence.strategy=sql-redis` configuration loads:
- ✅ SQL-specific beans
- ✅ JPA/Hibernate configuration
- ❌ NOT MongoDB beans
- ❌ NOT ElasticSearch beans

Configuration change → Different beans loaded → Different runtime behavior

---

**Status:** ✅ WORKING  
**Last Run:** October 26, 2025  
**Result:** 22/22 tests passing

