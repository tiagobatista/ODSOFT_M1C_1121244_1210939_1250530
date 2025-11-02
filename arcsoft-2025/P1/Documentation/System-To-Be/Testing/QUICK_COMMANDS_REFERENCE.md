# Quick Testing Commands - Reference Card

**Last Updated:** 2025-11-02

---

## 🎯 Mutation Testing (PIT)

### Run Mutation Tests
```cmd
mvn org.pitest:pitest-maven:mutationCoverage
```

### View Report
```cmd
start target\pit-reports\<timestamp>\index.html
```
Or navigate to: `target/pit-reports/<latest>/index.html`

### Run on Specific Class
```cmd
mvn org.pitest:pitest-maven:mutationCoverage -DtargetClasses=pt.psoft.g1.psoftg1.bookmanagement.model.Isbn
```

---

## 📊 Code Coverage (JaCoCo)

### Generate Coverage Report
```cmd
mvn clean test jacoco:report
```

### View Report
```cmd
start target\site\jacoco\index.html
```
Or navigate to: `target/site/jacoco/index.html`

### Run Tests + Coverage in One Command
```cmd
mvn clean test jacoco:report
```

---

## 🧪 Running Tests

### Run All Tests
```cmd
mvn clean test
```

### Run Specific Test Class
```cmd
mvn test -Dtest=IsbnTest
mvn test -Dtest=BookTest
mvn test -Dtest=AuthorTest
```

### Run All Domain Model Tests
```cmd
mvn test -Dtest="*Test" -DfailIfNoTests=false
```

### Run Configuration Tests
```cmd
mvn test -Dtest="*Configuration*Test"
```

### Run System Tests
```cmd
mvn test -Dtest=ElasticsearchSystemTest
```

### Run Integration Tests
```cmd
mvn test -Dtest="*IntegrationTest"
```

---

## 📈 Complete Test Suite

### Run Everything + Generate Reports
```cmd
# Run all tests with coverage
mvn clean test jacoco:report

# Then run mutation testing
mvn org.pitest:pitest-maven:mutationCoverage
```

---

## 🔍 View Results

### JaCoCo Coverage Report
- Location: `target/site/jacoco/index.html`
- Shows: Line coverage, branch coverage, cyclomatic complexity
- Green = covered, Yellow = partially covered, Red = not covered

### PIT Mutation Report  
- Location: `target/pit-reports/<timestamp>/index.html`
- Shows: Mutation coverage, survived mutants, test strength
- Green = all mutants killed, Yellow/Red = survived mutants

### Surefire Test Reports
- Location: `target/surefire-reports/`
- HTML: `target/surefire-reports/index.html`

---

## 🎯 2-Hour Sprint Workflow

### Step 1: Generate Baseline (5 min)
```cmd
mvn clean test jacoco:report
mvn org.pitest:pitest-maven:mutationCoverage
```

### Step 2: Analyze Reports (10 min)
- Open JaCoCo report → Find uncovered branches
- Open PIT report → Find survived mutants

### Step 3: Write Tests (60 min)
- Kill survived mutants
- Cover missing branches
- Run tests: `mvn test -Dtest=YourTest`

### Step 4: Re-measure (5 min)
```cmd
mvn clean test jacoco:report
mvn org.pitest:pitest-maven:mutationCoverage
```

### Step 5: Document (10 min)
- Take screenshots
- Note improvements
- Update documentation

---

## 📸 Screenshots for Report

### Must-Have Screenshots:
1. **PIT Summary Page** - showing mutation coverage %
2. **PIT Class Detail** - showing survived mutant example
3. **JaCoCo Summary** - showing line/branch coverage
4. **JaCoCo Class Detail** - showing branch coverage visualization

### How to Capture:
1. Open report in browser
2. Press `Windows + Shift + S` to screenshot
3. Save to: `arcsoft-2025/P1/Documentation/System-To-Be/Testing/evidence/`

---

## 🐛 Troubleshooting

### "No tests found"
```cmd
# Check test file naming - must end with "Test"
# Example: IsbnTest.java ✅  IsbnTests.java ❌
```

### "Mutation testing takes too long"
```cmd
# Increase timeout or reduce scope
mvn org.pitest:pitest-maven:mutationCoverage -DtimeoutFactor=2.0
```

### "OutOfMemoryError"
```cmd
set MAVEN_OPTS=-Xmx2048m
mvn org.pitest:pitest-maven:mutationCoverage
```

### "Cannot find PIT reports"
```cmd
# Check if mutation tests completed successfully
dir target\pit-reports /b
```

---

## ⏱️ Expected Execution Times

| Command | Time |
|---------|------|
| `mvn test` | 30-60s |
| `mvn test jacoco:report` | 40-70s |
| `mvn org.pitest:pitest-maven:mutationCoverage` | 3-10min |
| All tests + both reports | 5-12min |

---

## 📋 Checklist for 2-Hour Sprint

- [ ] Run initial mutation tests
- [ ] Run JaCoCo coverage
- [ ] Analyze PIT report
- [ ] Analyze JaCoCo report
- [ ] Write 3-5 new tests
- [ ] Re-run mutation tests
- [ ] Take screenshots
- [ ] Document improvements
- [ ] Update Testing README

---

## 🎓 Understanding the Metrics

### JaCoCo Metrics
- **Line Coverage:** % of code lines executed by tests
- **Branch Coverage:** % of if/switch branches tested
- **Cyclomatic Complexity:** Number of independent paths
- **Target:** >80% line, >70% branch

### PIT Metrics
- **Line Coverage:** % lines executed (prerequisite for mutation)
- **Mutation Coverage:** % mutations killed by tests
- **Test Strength:** Overall effectiveness
- **Target:** >70% mutation coverage

---

**Ready to start? Run this:**
```cmd
mvn clean test jacoco:report && mvn org.pitest:pitest-maven:mutationCoverage
```

Then open the reports and start analyzing! 🚀

