# Mutation Testing Guide - PIT

**Date:** 2025-11-02  
**Tool:** PIT (Pitest) - State-of-the-art mutation testing for Java

---

## What is Mutation Testing?

Mutation testing evaluates the quality of your tests by introducing small changes (mutations) to your code and checking if your tests catch them. If a test fails when the code is mutated, the mutant is "killed" (good!). If all tests pass despite the mutation, the mutant "survived" (bad - means your tests missed something).

**Example:**
```java
// Original code
if (isbn == null) {
    throw new IllegalArgumentException("ISBN cannot be null");
}

// Mutant 1: Removed null check (survived mutant = bad tests)
if (false) {  // PIT changes condition
    throw new IllegalArgumentException("ISBN cannot be null");
}

// If your tests don't catch this, you need better tests!
```

---

## Quick Start

### 1. Run Mutation Tests

```cmd
# Run mutation tests on domain models
mvn org.pitest:pitest-maven:mutationCoverage

# This will take 2-5 minutes depending on code size
```

### 2. View Results

```cmd
# Open the HTML report (Windows)
start target\pit-reports\<timestamp>\index.html

# Or navigate manually to:
target/pit-reports/<latest-timestamp>/index.html
```

### 3. Understand the Report

The report shows:
- **Line Coverage:** % of lines executed by tests
- **Mutation Coverage:** % of mutations killed by tests
- **Mutators:** Types of mutations applied (conditionals, returns, increments, etc.)
- **Survived Mutants:** Mutations that your tests didn't catch

**Good Scores:**
- ✅ Mutation Coverage > 70% (Good)
- ✅ Mutation Coverage > 80% (Excellent)
- ❌ Mutation Coverage < 60% (Needs improvement)

---

## Current Configuration

### Target Classes (what gets mutated)
- `pt.psoft.g1.psoftg1.*.model.*` - All domain model classes
- `pt.psoft.g1.psoftg1.shared.model.*` - Shared model classes

### Test Classes (what runs against mutants)
- `pt.psoft.g1.psoftg1.*.model.*Test` - All domain model tests
- `pt.psoft.g1.psoftg1.shared.model.*Test` - Shared model tests

### Excluded
- `*MapperImpl` - MapStruct generated classes
- `*Builder` - Lombok generated builders

---

## Understanding Mutation Operators

PIT applies different types of mutations:

### 1. Conditionals Boundary (CONDITIONALS_BOUNDARY)
```java
// Original
if (age > 18)

// Mutant
if (age >= 18)
```

### 2. Negate Conditionals (NEGATE_CONDITIONALS)
```java
// Original
if (name != null)

// Mutant
if (name == null)
```

### 3. Return Values (RETURN_VALS)
```java
// Original
return true;

// Mutant
return false;
```

### 4. Void Method Calls (VOID_METHOD_CALLS)
```java
// Original
list.add(item);

// Mutant
// Method call removed
```

### 5. Increments (INCREMENTS)
```java
// Original
counter++;

// Mutant
counter--;
```

---

## Improving Mutation Coverage

### Common Survived Mutants and Fixes

#### ❌ Problem: Null check not tested
```java
// Code
if (isbn == null) {
    throw new IllegalArgumentException("ISBN cannot be null");
}
```

**Fix:** Add test
```java
@Test
void ensureIsbnMustNotBeNull() {
    assertThrows(IllegalArgumentException.class, () -> new Isbn(null));
}
```

#### ❌ Problem: Boundary not tested
```java
// Code
if (length > 255) {
    throw new IllegalArgumentException("Too long");
}
```

**Fix:** Add boundary tests
```java
@Test
void ensureMaxLengthAccepted() {
    // Test exactly 255 characters
    assertDoesNotThrow(() -> new Title("x".repeat(255)));
}

@Test
void ensureMaxLengthExceededRejected() {
    // Test 256 characters
    assertThrows(IllegalArgumentException.class, 
        () -> new Title("x".repeat(256)));
}
```

#### ❌ Problem: Return value not verified
```java
// Code
public boolean isValid() {
    return checksum == calculateChecksum();
}
```

**Fix:** Test both true and false cases
```java
@Test
void ensureValidIsbnReturnsTrue() {
    Isbn isbn = new Isbn("9782826012092");
    assertTrue(isbn.isValid());
}

@Test
void ensureInvalidIsbnReturnsFalse() {
    assertThrows(IllegalArgumentException.class, 
        () -> new Isbn("9782826012099")); // Invalid checksum
}
```

---

## Step-by-Step: Killing Survived Mutants

### Step 1: Run Mutation Tests
```cmd
mvn org.pitest:pitest-maven:mutationCoverage
```

### Step 2: Open Report
```cmd
start target\pit-reports\<timestamp>\index.html
```

### Step 3: Find Survived Mutants
- Click on a class with survived mutants (red/yellow indicators)
- Look for lines highlighted in red or pink
- Read the mutation description

### Step 4: Write Test to Kill Mutant
Example for `Isbn.java`:

**Report shows:** "Negated conditional" survived on line 25
```java
// Line 25 - Original code
if (isbn.length() != 13 && isbn.length() != 10) {
    throw new IllegalArgumentException("Invalid length");
}
```

**Write test:**
```java
@Test
void ensureInvalidLengthRejected() {
    assertThrows(IllegalArgumentException.class, () -> new Isbn("123")); // Too short
    assertThrows(IllegalArgumentException.class, () -> new Isbn("12345678901234")); // Too long
}
```

### Step 5: Re-run and Verify
```cmd
mvn org.pitest:pitest-maven:mutationCoverage
```

Check if mutation coverage improved!

---

## Advanced: Run Specific Classes

### Test only specific class
```cmd
mvn org.pitest:pitest-maven:mutationCoverage -DtargetClasses=pt.psoft.g1.psoftg1.bookmanagement.model.Isbn
```

### Faster execution (fewer mutators)
```cmd
mvn org.pitest:pitest-maven:mutationCoverage -Dpitest.mutators=CONDITIONALS_BOUNDARY,NEGATE_CONDITIONALS
```

---

## Interpreting the Report

### Class Summary View
```
Class                  | Line Coverage | Mutation Coverage | Test Strength
--------------------- |---------------|-------------------|---------------
Isbn                  | 100%          | 85%               | 85%
Title                 | 100%          | 72%               | 72%
Book                  | 95%           | 65%               | 68%
```

**What this means:**
- **Line Coverage 100%:** All lines executed by tests (good!)
- **Mutation Coverage 85%:** 85% of mutants killed (excellent!)
- **Test Strength 72%:** 72% effective at catching real bugs

### Individual Class View
- **Green lines:** All mutants killed
- **Yellow lines:** Some mutants survived
- **Red lines:** No mutants killed
- **Pink background:** Line not covered by tests

---

## Integration with CI/CD

Add to your build process:

```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <executions>
        <execution>
            <id>pit-report</id>
            <phase>test</phase>
            <goals>
                <goal>mutationCoverage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Or run manually:
```cmd
mvn clean test org.pitest:pitest-maven:mutationCoverage
```

---

## Troubleshooting

### Issue: "No mutations found"
**Solution:** Check target classes pattern matches your package structure

### Issue: Tests take too long
**Solution:** Reduce scope or increase timeout
```cmd
mvn org.pitest:pitest-maven:mutationCoverage -DtimeoutFactor=2.0
```

### Issue: OutOfMemoryError
**Solution:** Increase Maven memory
```cmd
set MAVEN_OPTS=-Xmx2048m
mvn org.pitest:pitest-maven:mutationCoverage
```

---

## Best Practices

### ✅ DO:
- Run mutation tests regularly (weekly minimum)
- Focus on domain model classes first
- Aim for >75% mutation coverage
- Write tests to kill survived mutants
- Include mutation testing in CI pipeline

### ❌ DON'T:
- Don't aim for 100% mutation coverage (diminishing returns)
- Don't mutate generated code (MapStruct, Lombok)
- Don't mutate infrastructure code (repositories, controllers)
- Don't ignore survived mutants - investigate each one

---

## Quick Reference Commands

```cmd
# Basic run
mvn org.pitest:pitest-maven:mutationCoverage

# Clean + run
mvn clean test org.pitest:pitest-maven:mutationCoverage

# View report (Windows)
start target\pit-reports\<timestamp>\index.html

# Run on specific package
mvn org.pitest:pitest-maven:mutationCoverage -DtargetClasses=pt.psoft.g1.psoftg1.bookmanagement.model.*

# Generate XML report for CI
mvn org.pitest:pitest-maven:mutationCoverage -DoutputFormats=XML,HTML
```

---

## Expected Results (Initial Run)

Based on current test coverage, expect:
- **Line Coverage:** 85-95% (existing tests are good)
- **Mutation Coverage:** 55-70% (typical for first run)
- **Survived Mutants:** 20-40 (opportunities for improvement)

**Goal for 2-hour session:** Increase mutation coverage from ~60% to >75%

---

## For Your Report

Include in your project documentation:
1. Screenshot of mutation testing report summary
2. Example of a survived mutant and the test you wrote to kill it
3. Before/After mutation coverage scores
4. Analysis of what mutation testing revealed about test quality

---

**Ready to start?** Run this command:
```cmd
mvn org.pitest:pitest-maven:mutationCoverage
```

**Next:** Open the report and let's identify which mutants need killing! 🎯

