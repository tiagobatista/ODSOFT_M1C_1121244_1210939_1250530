# Documentation Index

## Complete Documentation Map

This index provides a comprehensive map of all documentation in this project.

---

## 🎯 Getting Started (Start Here!)

1. **[README.md](../README.md)** - Project overview and quick start
2. **[DATABASE_SWITCHING_QUICK_REF.md](DATABASE_SWITCHING_QUICK_REF.md)** - One-page cheat sheet
3. **[SETUP_COMPLETE.md](SETUP_COMPLETE.md)** - Initial setup summary

---

## 📘 Configuration Guides

### Primary Configuration Documentation

| Document | Purpose | When to Use |
|----------|---------|-------------|
| **[DATABASE_SWITCHING_GUIDE.md](DATABASE_SWITCHING_GUIDE.md)** | Step-by-step guide for switching between database providers | When you need to change from SQL to MongoDB/ElasticSearch |
| **[PERSISTENCE_CONFIG.md](PERSISTENCE_CONFIG.md)** | Detailed persistence configuration reference | When configuring database settings, TTL, caching |
| **[REDIS_QUICK_REFERENCE.md](REDIS_QUICK_REFERENCE.md)** | Redis caching configuration and usage | When working with Redis caching |

### Quick References

| Document | Purpose |
|----------|---------|
| **[DATABASE_SWITCHING_QUICK_REF.md](DATABASE_SWITCHING_QUICK_REF.md)** | One-page database switching cheat sheet |
| **[PERSISTENCE_TESTS_QUICK_REF.md](PERSISTENCE_TESTS_QUICK_REF.md)** | Quick test execution reference |

---

## 🧪 Testing Documentation

### Test Guides

| Document | Tests Covered | Status |
|----------|--------------|--------|
| **[CONFIGURATION_TESTS_SUMMARY.md](CONFIGURATION_TESTS_SUMMARY.md)** | 22 configuration tests | ✅ All Passing |
| **[PERSISTENCE_TESTS_COMMANDS.md](PERSISTENCE_TESTS_COMMANDS.md)** | All test execution commands | ✅ Complete |
| **[PERSISTENCE_TESTS_SUMMARY.md](PERSISTENCE_TESTS_SUMMARY.md)** | Persistence testing overview | ✅ Complete |

### Test Classes

| Test Class | Count | File |
|------------|-------|------|
| SqlRedisProfileConfigurationTest | 9 tests | `src/test/java/.../configuration/SqlRedisProfileConfigurationTest.java` |
| PersistenceConfigTest | 6 tests | `src/test/java/.../configuration/PersistenceConfigTest.java` |
| SqlRepositoryProfileTest | 7 tests | `src/test/java/.../configuration/SqlRepositoryProfileTest.java` |

---

## 🔧 Implementation Documentation

### Technical Implementation

| Document | Focus | Audience |
|----------|-------|----------|
| **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** | Technical architecture & implementation | Developers |
| **[FINAL_DATABASE_AGNOSTIC_FIX.md](FINAL_DATABASE_AGNOSTIC_FIX.md)** | Database-agnostic design fixes | Technical reviewers |
| **[REDIS_STATUS_ASSESSMENT.md](REDIS_STATUS_ASSESSMENT.md)** | Redis implementation status and analysis | Technical leads |

### Status & Progress

| Document | Purpose |
|----------|---------|
| **[FINAL_STATUS.md](FINAL_STATUS.md)** | Overall implementation status |
| **[SUCCESS_SUMMARY.md](SUCCESS_SUMMARY.md)** | Achievement summary |
| **[ADD_REDIS_UPDATE_SUMMARY.md](ADD_REDIS_UPDATE_SUMMARY.md)** | Redis ADD report update details |

---

## 📚 Architecture & Design

### Main Architecture Documents

| Document | Type | Location |
|----------|------|----------|
| **ADD Report** | Architecture Decision Document | `arcsoft-2025/P1/Documentation/Report/report-p1.md` |
| **Logic View** | Architecture diagram | `arcsoft-2025/P1/Documentation/System-To-Be/Views/Logic/` |
| **Implementation View** | Code structure | `arcsoft-2025/P1/Documentation/System-To-Be/Views/Implementation/` |

---

## 🐛 Troubleshooting & Fixes

### Bug Fixes & Solutions

| Document | Issue Addressed |
|----------|----------------|
| **[AUTHENTICATION_FIXES.md](AUTHENTICATION_FIXES.md)** | Authentication system fixes |
| **[AUTHENTICATION_GUIDE.md](AUTHENTICATION_GUIDE.md)** | Authentication usage guide |
| **[TOSTRING_FIX.md](TOSTRING_FIX.md)** | ToString implementation fixes |
| **[FINAL_DATABASE_AGNOSTIC_FIX.md](FINAL_DATABASE_AGNOSTIC_FIX.md)** | Database abstraction fixes |

### Configuration Issues

| Document | Solution |
|----------|----------|
| **[TROUBLESHOOTING_CURL.md](TROUBLESHOOTING_CURL.md)** | CURL command issues |
| **[QUICK_403_FIX.md](QUICK_403_FIX.md)** | HTTP 403 Forbidden errors |

---

## 📋 Reference Documents

### Credentials & Endpoints

| Document | Contains |
|----------|----------|
| **[CORRECT_CREDENTIALS.md](CORRECT_CREDENTIALS.md)** | Valid test credentials |
| **[ENDPOINTS_GUIDE.md](ENDPOINTS_GUIDE.md)** | API endpoint documentation |
| **[ENDPOINT_PERMISSIONS.md](ENDPOINT_PERMISSIONS.md)** | Role-based access control |

---

## 🎓 Learning & Help

### Getting Help

| Document | Help With |
|----------|-----------|
| **[HELP.md](HELP.md)** | General help and resources |
| **[DATABASE_SWITCHING_GUIDE.md](DATABASE_SWITCHING_GUIDE.md)** | Switching databases |
| **[TROUBLESHOOTING section](DATABASE_SWITCHING_GUIDE.md#troubleshooting)** | Common problems |

---

## 📊 Document Categories

### By Purpose

#### Configuration & Setup (8 documents)
- DATABASE_SWITCHING_GUIDE.md ⭐ **START HERE**
- DATABASE_SWITCHING_QUICK_REF.md
- PERSISTENCE_CONFIG.md
- REDIS_QUICK_REFERENCE.md
- SETUP_COMPLETE.md
- AUTHENTICATION_GUIDE.md
- ENDPOINTS_GUIDE.md
- ENDPOINT_PERMISSIONS.md

#### Testing (5 documents)
- CONFIGURATION_TESTS_SUMMARY.md ⭐ **MAIN TEST GUIDE**
- PERSISTENCE_TESTS_COMMANDS.md
- PERSISTENCE_TESTS_QUICK_REF.md
- PERSISTENCE_TESTS_SUMMARY.md
- FIX_APPLIED.md

#### Implementation & Technical (6 documents)
- IMPLEMENTATION_SUMMARY.md
- FINAL_DATABASE_AGNOSTIC_FIX.md
- REDIS_STATUS_ASSESSMENT.md
- ADD_REDIS_UPDATE_SUMMARY.md
- TOSTRING_FIX.md
- PERSISTENCE_CONFIG.md

#### Status & Progress (4 documents)
- FINAL_STATUS.md
- SUCCESS_SUMMARY.md
- SECURITY_BUG_FOUND.md
- AUTHENTICATION_FIXES.md

#### Troubleshooting (4 documents)
- TROUBLESHOOTING_CURL.md
- QUICK_403_FIX.md
- CORRECT_CREDENTIALS.md
- HELP.md

---

## 🔍 Find Documentation By Task

### "I want to..."

| Task | Go To |
|------|-------|
| **Switch from SQL to MongoDB** | [DATABASE_SWITCHING_GUIDE.md](DATABASE_SWITCHING_GUIDE.md) |
| **Enable Redis caching** | [REDIS_QUICK_REFERENCE.md](REDIS_QUICK_REFERENCE.md) |
| **Run configuration tests** | [CONFIGURATION_TESTS_SUMMARY.md](CONFIGURATION_TESTS_SUMMARY.md) |
| **Fix 403 Forbidden error** | [QUICK_403_FIX.md](QUICK_403_FIX.md) |
| **Understand architecture** | [ADD Report](../arcsof/P1/Documentation/Report/report-p1.md) |
| **See implementation status** | [FINAL_STATUS.md](FINAL_STATUS.md) |
| **Get valid credentials** | [CORRECT_CREDENTIALS.md](CORRECT_CREDENTIALS.md) |
| **Learn about API endpoints** | [ENDPOINTS_GUIDE.md](ENDPOINTS_GUIDE.md) |
| **Troubleshoot Redis** | [REDIS_STATUS_ASSESSMENT.md](REDIS_STATUS_ASSESSMENT.md) |
| **Quick command reference** | [DATABASE_SWITCHING_QUICK_REF.md](DATABASE_SWITCHING_QUICK_REF.md) |

---

## 📈 Documentation Statistics

- **Total Markdown Documents**: 30+
- **Configuration Guides**: 8
- **Test Documentation**: 5
- **Implementation Docs**: 6
- **Quick References**: 4
- **Troubleshooting Guides**: 4

---

## 🎯 Recommended Reading Order

### For New Team Members:
1. README.md
2. DATABASE_SWITCHING_QUICK_REF.md
3. CONFIGURATION_TESTS_SUMMARY.md
4. AUTHENTICATION_GUIDE.md
5. ENDPOINTS_GUIDE.md

### For Reviewers/Evaluators:
1. README.md
2. FINAL_STATUS.md
3. CONFIGURATION_TESTS_SUMMARY.md
4. DATABASE_SWITCHING_GUIDE.md
5. ADD Report (arcsoft-2025/P1/Documentation/Report/report-p1.md)

### For Developers Continuing This Work:
1. IMPLEMENTATION_SUMMARY.md
2. FINAL_DATABASE_AGNOSTIC_FIX.md
3. DATABASE_SWITCHING_GUIDE.md
4. PERSISTENCE_CONFIG.md
5. CONFIGURATION_TESTS_SUMMARY.md

---

## 🔗 External References

### Postman Collection
- Location: `Docs/Psoft-G1.postman_collection.json`
- Environment: `Docs/Psoft-G1.postman_environment.json`

### Project PDFs
- `ARQSOFT 2025-2026 Project 1 for Students v1.1.pdf`
- `PSOFT_LETI_assignment_2023-2024.pdf`

### Code Structure
- Source: `src/main/java/pt/psoft/g1/psoftg1/`
- Tests: `src/test/java/pt/psoft/g1/psoftg1/`
- Resources: `src/main/resources/`

---

## 📝 Document Update Log

| Date | Document | Change |
|------|----------|--------|
| 2025-10-26 | DATABASE_SWITCHING_GUIDE.md | ✅ Created |
| 2025-10-26 | DATABASE_SWITCHING_QUICK_REF.md | ✅ Created |
| 2025-10-26 | CONFIGURATION_TESTS_SUMMARY.md | ✅ Created |
| 2025-10-26 | REDIS_STATUS_ASSESSMENT.md | ✅ Created |
| 2025-10-26 | ADD_REDIS_UPDATE_SUMMARY.md | ✅ Created |
| 2025-10-26 | README.md | ✅ Updated with links |
| 2025-10-26 | report-p1.md (ADD Report) | ✅ Updated Redis section |

---

## 💡 Tips for Using This Documentation

### Quick Lookup
- Use Ctrl+F to search this index
- Check the "I want to..." section for task-based navigation
- Use Quick Reference docs for fast answers

### Deep Dive
- Start with comprehensive guides for full understanding
- Reference implementation docs for technical details
- Check ADD Report for architectural decisions

### Troubleshooting
- Check Quick References first
- Then troubleshooting guides
- Finally, comprehensive guides for context

---

**Last Updated**: 2025-10-26  
**Maintained By**: Project Team  
**Status**: ✅ Complete and current

