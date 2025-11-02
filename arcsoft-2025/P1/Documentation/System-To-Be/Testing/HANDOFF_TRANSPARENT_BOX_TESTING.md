# Handoff Document - Transparent-Box Testing Assignment

**Date:** 2025-11-02  
**From:** Testing Setup Team  
**To:** Colleague (Transparent-Box Testing)

---

## 🎯 Your Assignment: Transparent-Box Testing

You've been assigned to implement **Functional Transparent-Box Tests with SUT = Domain Classes**.

### What is Transparent-Box Testing?

Transparent-box (white-box) testing involves testing based on **internal code structure**, not just external behavior. You'll design tests to achieve maximum **branch coverage** and **path coverage**.

---

## 📁 Project Structure

```
src/
├── main/java/pt/psoft/g1/psoftg1/
│   ├── bookmanagement/model/        # Domain classes to test
│   ├── authormanagement/model/
│   ├── lendingmanagement/model/
│   ├── readermanagement/model/
│   ├── genremanagement/model/
│   └── shared/model/
└── test/java/pt/psoft/g1/psoftg1/
    ├── bookmanagement/model/        # Your test files go here
    ├── authormanagement/model/
    └── ... (same structure)
```

---

## 🛠️ Tools Already Configured

### 1. JaCoCo Code Coverage
**Purpose:** Identify uncovered branches and paths  
**Already configured:** Yes ✅  
**Report location:** `target/site/jacoco/index.html`

### 2. Maven Test Framework
**Framework:** JUnit 5  
**Already configured:** Yes ✅

---

## 🚀 Quick Start Guide

### Step 1: Generate Coverage Baseline (5 min)

```cmd
# Navigate to project root
cd F:\repos\domingo_3\MEI-ARCSOFT-2025-2026-1191577-1210939-1250530

# Run all tests and generate coverage report
mvn clean test jacoco:report
```

### Step 2: Analyze Coverage Report (10 min)

```cmd
# Open the coverage report
start target\site\jacoco\index.html
```

**What to look for:**
- **Green lines:** Fully covered
- **Yellow lines:** Partially covered (some branches missed)
- **Red lines:** Not covered at all

**Focus on:**
- Domain model classes (value objects and entities)
- Classes with yellow/red indicators
- Branch coverage < 80%

### Step 3: Design Transparent-Box Tests (Main Task)

For each domain class, analyze the **internal implementation** and write tests to cover:

#### a) Branch Coverage
Every `if`, `else`, `switch` branch should be tested

**Example - Isbn class:**
```java
// Code structure
public Isbn(String isbn) {
    if (isbn == null) {              // Branch 1
        throw new IllegalArgumentException();
    }
    if (isbn.length() == 13) {       // Branch 2a
        // validate ISBN-13
    } else if (isbn.length() == 10) { // Branch 2b
        // validate ISBN-10
    } else {                          // Branch 2c
        throw new IllegalArgumentException();
    }
}

// Transparent-box tests (one for each branch)
@Test
void testNullIsbn_Branch1() {
    assertThrows(IllegalArgumentException.class, () -> new Isbn(null));
}

@Test
void testIsbn13_Branch2a() {
    assertDoesNotThrow(() -> new Isbn("9782826012092"));
}

@Test
void testIsbn10_Branch2b() {
    assertDoesNotThrow(() -> new Isbn("8175257660"));
}

@Test
void testInvalidLength_Branch2c() {
    assertThrows(IllegalArgumentException.class, () -> new Isbn("123"));
}
```

#### b) Path Coverage
Test all possible execution paths through the code

**Example - Complex validation:**
```java
// Code with multiple paths
public void validate() {
    if (condition1) {
        if (condition2) {
            // Path 1: true, true
        } else {
            // Path 2: true, false
        }
    } else {
        // Path 3: false
    }
}

// Tests for each path
@Test void testPath1_TrueTrue() { }
@Test void testPath2_TrueFalse() { }
@Test void testPath3_False() { }
```

#### c) Boundary Testing
Test at exact boundaries, not just valid/invalid

**Example:**
```java
// Code
if (length > 255) throw new IllegalArgumentException();

// Boundary tests
@Test void testMaxLength255_OnBoundary() {
    assertDoesNotThrow(() -> new Title("x".repeat(255))); // Exactly 255
}

@Test void testMaxLength256_OverBoundary() {
    assertThrows(IllegalArgumentException.class, 
        () -> new Title("x".repeat(256))); // Exactly 256
}
```

---

## 📋 Classes to Test (Priority Order)

### Priority 1: Value Objects (Start here - simpler)
1. `Isbn.java` - ISBN validation logic
2. `Title.java` - Title validation
3. `Description.java` - Description validation
4. `Bio.java` - Bio validation
5. `Name.java` - Name validation
6. `PhoneNumber.java` - Phone validation
7. `BirthDate.java` - Date validation
8. `LendingNumber.java` - Lending number format

### Priority 2: Entities (More complex)
1. `Book.java` - Book entity logic
2. `Author.java` - Author entity logic
3. `Lending.java` - Lending business logic
4. `Reader.java` - Reader entity logic
5. `Genre.java` - Genre entity logic

---

## 🎯 Success Criteria

### Minimum Goals:
- [ ] All value objects have >85% branch coverage
- [ ] At least 3 entity classes have >80% branch coverage
- [ ] Document test design approach for 2-3 classes

### Target Goals:
- [ ] All value objects have >90% branch coverage
- [ ] All entities have >80% branch coverage
- [ ] JaCoCo report shows improvement from baseline

### Stretch Goals:
- [ ] Overall branch coverage >85%
- [ ] Document complete path coverage analysis
- [ ] Create coverage improvement report

---

## 📖 Test Naming Convention

Use descriptive names that indicate **which path/branch** is being tested:

```java
// ❌ Bad (opaque-box style)
@Test void testValidation() { }

// ✅ Good (transparent-box style)
@Test void testValidation_NullInput_ThrowsException() { }
@Test void testValidation_ValidLength_Success() { }
@Test void testValidation_OverMaxLength_ThrowsException() { }
@Test void testValidation_Boundary255_Success() { }
@Test void testValidation_Boundary256_ThrowsException() { }
```

---

## 🔍 Using JaCoCo Effectively

### Viewing Coverage for a Specific Class

1. Open: `target/site/jacoco/index.html`
2. Navigate: `pt.psoft.g1.psoftg1` > `bookmanagement` > `model` > `Isbn.java`
3. View: Source code with coverage highlighting

**Color coding:**
- **Green background:** Line fully covered
- **Yellow background:** Branch partially covered (some paths not tested)
- **Red background:** Line not covered
- **Red diamond:** Branch not covered
- **Yellow diamond:** Branch partially covered

### Understanding Branch Coverage Metrics

```
Isbn.java
- Line Coverage: 95% (19/20 lines)
- Branch Coverage: 70% (7/10 branches)
```

**This means:** 
- Most lines are executed, BUT
- 3 out of 10 branches (if/else paths) are not tested
- You need to write tests for those 3 missing branches

---

## 📊 Reporting Your Work

### Create a Test Coverage Report

Document your work in: `TRANSPARENT_BOX_TEST_REPORT.md`

Include:
1. **Baseline Metrics** (before your work)
   - Screenshot of JaCoCo summary
   - Branch coverage per class

2. **Analysis** (which branches were missing)
   - List of uncovered branches found
   - Explanation of why they were missed

3. **Test Design** (how you designed tests)
   - Path analysis for 2-3 complex classes
   - Branch diagram (if applicable)
   - Test case mapping to branches

4. **Final Metrics** (after your work)
   - Screenshot of JaCoCo summary
   - Comparison table showing improvement

5. **Example** (detailed example of one class)
   - Show code with branches
   - Show tests covering each branch
   - Show before/after JaCoCo screenshots

---

## 🚨 Common Pitfalls to Avoid

### ❌ Don't: Just aim for 100% line coverage
**Why:** You can execute every line without testing every branch

```java
// All lines executed, but only 50% branch coverage
@Test
void badTest() {
    if (condition) doSomething();  // Only true path tested
    else doSomethingElse();        // false path never tested
}
```

### ❌ Don't: Test generated code
**Why:** Lombok/MapStruct generated code doesn't need branch testing

Skip:
- Getters/setters generated by Lombok
- Builders generated by Lombok
- Mapper implementations from MapStruct

### ❌ Don't: Confuse branch coverage with mutation coverage
**Why:** They're different metrics!

- **Branch Coverage:** Did you execute this path?
- **Mutation Coverage:** Did you verify the behavior?

---

## 💡 Tips for Success

### 1. Start with the Smallest Classes
Begin with simple value objects like `Isbn` or `Title` to understand the workflow before tackling complex entities.

### 2. Use Coverage Report Iteratively
```cmd
# Write a few tests
mvn test -Dtest=IsbnTest

# Check coverage
mvn jacoco:report
start target\site\jacoco\index.html

# Repeat until target reached
```

### 3. Document While You Work
Don't wait until the end! Take screenshots and notes as you improve coverage.

### 4. Ask Questions
If JaCoCo shows a branch but you can't figure out how to reach it, document it and ask.

---

## 🎓 Learning Objectives

By completing this assignment, you'll demonstrate:

1. **Understanding of code structure** - Ability to read and analyze internal implementation
2. **Test design skills** - Creating tests based on code paths, not just requirements
3. **Quality metrics** - Using tools to measure test effectiveness
4. **Documentation** - Explaining test strategy and improvements

---

## 📞 Support & Questions

### Documentation Available:
- `arcsoft-2025/P1/Documentation/System-To-Be/Testing/README.md`
- `arcsoft-2025/P1/Documentation/System-To-Be/Testing/QUICK_COMMANDS_REFERENCE.md`
- `arcsoft-2025/P1/Documentation/System-To-Be/Testing/TEST_COVERAGE_ANALYSIS.md`

### Quick Commands:
```cmd
# Run all tests
mvn clean test

# Run tests for specific class
mvn test -Dtest=IsbnTest

# Generate coverage report
mvn jacoco:report

# View report
start target\site\jacoco\index.html
```

---

## ✅ Checklist Before You Start

- [ ] Read this entire document
- [ ] Run baseline coverage: `mvn clean test jacoco:report`
- [ ] Open and explore JaCoCo report
- [ ] Identify 2-3 classes to start with
- [ ] Set up your development environment

---

## ✅ Checklist When You Finish

- [ ] All priority 1 classes have >85% branch coverage
- [ ] Created `TRANSPARENT_BOX_TEST_REPORT.md`
- [ ] Taken before/after screenshots
- [ ] Committed all test code
- [ ] Re-run final coverage: `mvn clean test jacoco:report`
- [ ] Verified all tests pass

---

**Estimated Time:** 3-4 hours  
**Difficulty:** Medium  
**Value:** HIGH - Demonstrates advanced testing skills

**Good luck! Remember: Quality over quantity. It's better to have perfect branch coverage on 5 classes than partial coverage on 15 classes.**

---

**Questions? Check the documentation or ask the team!**

