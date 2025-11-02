# 2-Hour Testing Sprint - Action Plan

**Date:** 2025-11-02  
**Duration:** 2 hours  
**Goal:** Maximize test coverage improvements with available time

---

## ✅ Completed (10 minutes)

1. ✅ Added PIT Mutation Testing plugin to pom.xml
2. ✅ Created mutation testing guide
3. ✅ Created test coverage analysis
4. ✅ Organized documentation in Testing directory
5. ✅ Started initial mutation test run

---

## 🎯 Current Priorities (Remaining ~1h 50min)

### Priority 1: Mutation Testing (60 minutes) ⚡ HIGH IMPACT

**Status:** ⏳ Running initial mutation tests...

**Next Steps:**
1. **Wait for mutation report** (5-10 min) - Currently running
2. **Analyze results** (10 min)
   - Open: `target/pit-reports/<timestamp>/index.html`
   - Identify classes with low mutation coverage
   - Find survived mutants (low-hanging fruit)
3. **Kill survived mutants** (40 min)
   - Focus on domain classes with <70% mutation coverage
   - Write tests for survived mutants
   - Re-run: `mvn org.pitest:pitest-maven:mutationCoverage`
   - Document improvements

**Expected Outcome:**
- Mutation coverage report generated ✅
- Increase mutation coverage from ~60% to >75%
- At least 3-5 new tests added to kill mutants
- Screenshot evidence for report

---

### Priority 2: Transparent-Box Tests (40 minutes) ⚡ MEDIUM IMPACT

**Goal:** Add white-box tests based on code structure analysis

**Action Items:**
1. **Generate code coverage report** (5 min)
   ```cmd
   mvn clean test jacoco:report
   ```
   Open: `target/site/jacoco/index.html`

2. **Identify uncovered branches** (10 min)
   - Look for yellow/red highlighted code
   - Focus on domain classes with <90% branch coverage
   - Document specific branches to test

3. **Write transparent-box tests** (25 min)
   - Pick 2-3 classes with complex logic (e.g., Isbn, Lending, Book)
   - Write tests for each branch/path
   - Document test design approach (path coverage, branch coverage)

**Expected Outcome:**
- JaCoCo report generated
- 5-10 new transparent-box tests
- Improved branch coverage by 5-10%
- Documentation of approach

---

### Priority 3: Documentation (10 minutes) 📝

**Update Testing README with new information:**
1. Add mutation testing section
2. Add transparent-box testing section
3. Include commands to run tests
4. Add screenshots/evidence

---

## Quick Commands Reference

### Run Mutation Tests
```cmd
cd F:\repos\domingo_3\MEI-ARCSOFT-2025-2026-1191577-1210939-1250530
mvn org.pitest:pitest-maven:mutationCoverage
```

### Generate Coverage Report
```cmd
mvn clean test jacoco:report
```

### Run Specific Test
```cmd
mvn test -Dtest=IsbnTest
```

### Run All Domain Model Tests
```cmd
mvn test -Dtest="*Test" -DfailIfNoTests=false
```

---

## Time Allocation

| Task | Time | Status |
|------|------|--------|
| Setup & Config | 10 min | ✅ Done |
| Mutation Testing | 60 min | ⏳ In Progress |
| Transparent-Box Tests | 40 min | 📋 Planned |
| Documentation | 10 min | 📋 Planned |
| **Total** | **120 min** | |

---

## Key Deliverables

### For Project Report:

1. **Mutation Testing Evidence:**
   - [ ] PIT mutation report screenshot (summary page)
   - [ ] Example of survived mutant + test to kill it
   - [ ] Before/after mutation coverage metrics

2. **Transparent-Box Testing Evidence:**
   - [ ] JaCoCo coverage report screenshot
   - [ ] Branch coverage metrics
   - [ ] Example of path-based test design

3. **Documentation:**
   - [ ] Updated Testing README
   - [ ] Test coverage analysis document
   - [ ] Mutation testing guide

4. **Code:**
   - [ ] New mutation-killing tests
   - [ ] New transparent-box tests
   - [ ] Updated pom.xml with PIT plugin

---

## Success Criteria (2 hours)

### Minimum (Must Have):
- ✅ Mutation testing configured and running
- ⏳ Mutation coverage report generated
- 🎯 At least 3 new tests to kill survived mutants
- 📊 Evidence for report (screenshots, metrics)

### Target (Should Have):
- 🎯 Mutation coverage >70%
- 🎯 5-10 transparent-box tests added
- 📊 JaCoCo coverage report
- 📝 Documentation updated

### Stretch (Nice to Have):
- 🏆 Mutation coverage >75%
- 🏆 Branch coverage >90% for key domain classes
- 📝 Comprehensive test design documentation

---

## Current Status Check

**Time Elapsed:** ~10 minutes  
**Time Remaining:** ~110 minutes

**Completed:**
✅ PIT plugin configured  
✅ Documentation created  
✅ Files organized  
⏳ Mutation tests running...

**Next Action:** 
Wait for mutation test completion (~5 more minutes), then analyze report and start killing mutants!

---

## Notes

- Focus on **high-impact, quick wins**
- Mutation testing is the **critical missing piece**
- Document everything for the report
- Take screenshots as you go
- Don't aim for perfection - aim for **demonstrable improvement**

---

**Let's maximize the impact in the time we have!** 🚀

