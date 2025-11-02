# ⚠️ SECURITY CONFIG ANALYSIS - Issues Found!

## 🔍 Current State Analysis

Analyzed `SecurityConfig.java` on 2025-10-26

---

## ❌ CRITICAL ISSUE: DUPLICATE ENDPOINT RULES

Spring Security processes authorization rules **in order**, and the **FIRST matching rule wins**. Having duplicate rules causes unpredictable behavior!

### 🔴 Found Duplicates:

#### 1. `/api/readers/top5ByGenre` - **DUPLICATED!**
```java
Line 155: .requestMatchers(HttpMethod.GET,"/api/readers/top5ByGenre").hasAuthority(Role.LIBRARIAN)
Line 160: .requestMatchers(HttpMethod.GET,"/api/readers/top5ByGenre").hasAuthority(Role.LIBRARIAN)
```
**Impact:** Redundant, but same permission so no conflict

#### 2. `/api/lendings/overdue` - **DUPLICATED!**
```java
Line 173: .requestMatchers(HttpMethod.GET,"/api/lendings/overdue").hasAuthority(Role.LIBRARIAN)
Line 177: .requestMatchers(HttpMethod.GET,"/api/lendings/overdue").hasAuthority(Role.LIBRARIAN)
```
**Impact:** Redundant, but same permission so no conflict

---

## ⚠️ POTENTIAL ORDERING ISSUES

### Authors Endpoints - **POSSIBLE CONFLICTS**

```java
// Line 137 - More specific (with path variable)
.requestMatchers(HttpMethod.GET,"/api/authors/{authorNumber}").hasAnyAuthority(Role.READER, Role.LIBRARIAN)

// Line 138 - More general (no path variable)  
.requestMatchers(HttpMethod.GET,"/api/authors").hasAnyAuthority(Role.READER, Role.LIBRARIAN)
```

✅ **Currently OK** - More specific comes first (correct order)

However, based on ENDPOINT_PERMISSIONS.md:
- `/api/authors/{authorNumber}/books` should be **READER only**
- `/api/authors/top5` should be **READER only**
- `/api/authors/{authorNumber}/coauthors` should be **READER only**

But in SecurityConfig:
```java
Line 139: .requestMatchers(HttpMethod.GET,"/api/authors/{authorNumber}/books").hasAuthority(Role.READER)  ✅
Line 140: .requestMatchers(HttpMethod.GET,"/api/authors/top5").hasAuthority(Role.READER)  ✅
Line 142: .requestMatchers(HttpMethod.GET,"/api/authors/{authorNumber}/coauthors").hasAuthority(Role.READER)  ✅
```

These are correct!

---

## 🔍 Comparing with ENDPOINT_PERMISSIONS.md

### ✅ Correct Permissions:

| Endpoint | Expected | Actual | Status |
|----------|----------|--------|--------|
| `GET /api/books` | READER + LIBRARIAN | `hasAnyAuthority(LIBRARIAN, READER)` | ✅ |
| `GET /api/books/top5` | LIBRARIAN only | `hasAuthority(LIBRARIAN)` | ✅ |
| `GET /api/authors/top5` | READER only | `hasAuthority(READER)` | ✅ |
| `GET /api/readers/top5` | LIBRARIAN only | `hasAuthority(LIBRARIAN)` | ✅ |

---

## 🐛 THE REAL PROBLEM

Based on your description: **"some endpoints working but others stopped"**

### Hypothesis 1: Session Management Conflict ❌
```java
Line 102: .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
Line 108: .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
```

**PROBLEM:** `sessionManagement` is configured **TWICE** with **DIFFERENT** settings!
- First: `STATELESS` (for JWT/OAuth)
- Second: `IF_REQUIRED` (for Basic Auth + H2)

**Spring will use the LAST one** → `IF_REQUIRED`

This might cause:
- JWT tokens not working properly
- Session cookies being created unexpectedly
- Authentication state not being properly managed

---

### Hypothesis 2: CSRF Disabled Twice ❌
```java
Line 96:  http = http.cors(Customizer.withDefaults()).csrf(csrf -> csrf.disable());
Line 107: .csrf(csrf -> csrf.disable())
```

**PROBLEM:** CSRF is disabled twice (redundant but harmless)

---

### Hypothesis 3: Authorization Chain Split ❌

The authorization rules are split into two sections:
1. Lines 96-104: First `http =` assignments
2. Lines 107-186: The main authorization chain

This creates a **confusing configuration structure** where settings might override each other.

---

## ✅ RECOMMENDED FIXES

### Fix 1: Remove Duplicate Rules
Delete lines:
- Line 160 (duplicate `top5ByGenre`)
- Line 177 (duplicate `overdue`)

### Fix 2: Fix Session Management Conflict ⭐ **CRITICAL**
**Choose ONE strategy:**

**Option A: For JWT/OAuth (Stateless)**
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**Option B: For Basic Auth + Session (Stateful)**
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
```

**Current app uses BOTH Basic Auth AND JWT**, so should use:
```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```
(JWT doesn't need sessions, Basic Auth can work stateless)

### Fix 3: Consolidate Configuration
Remove the duplicate/conflicting configuration at the top (lines 96-104)

---

## 🎯 ROOT CAUSE ANALYSIS

**Why some endpoints work and others don't:**

1. **Session confusion**: `STATELESS` vs `IF_REQUIRED` causes auth to work inconsistently
2. **Duplicate rules**: While not breaking, they indicate configuration mess
3. **Config override**: The two separate configuration blocks might override each other

**Which endpoints are probably broken:**
- Endpoints tested with **JWT tokens** (if sessions are enabled)
- Endpoints tested with **Basic Auth** (if sessions are stateless but cookie expected)
- Endpoints requiring **complex authorization** (if session state is lost)

---

## 🛠️ IMMEDIATE ACTION NEEDED

**Before changing database config, FIX THE SECURITY CONFIG:**

1. ✅ Remove duplicate endpoint rules
2. ✅ Fix session management conflict (choose STATELESS)
3. ✅ Consolidate configuration into ONE clear chain
4. ✅ Test with BOTH Basic Auth AND JWT

---

## 📝 Quick Test to Confirm Issue

```bash
# Test 1: Basic Auth (should work)
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books

# Test 2: Get JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/public/login -H "Content-Type: application/json" -d '{"username":"manuel@gmail.com","password":"Manuelino123!"}' | jq -r '.token')

# Test 3: Use JWT token (might fail if session conflict)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/books
```

If Test 1 works but Test 3 fails → **Session management conflict confirmed!**

---

**Status:** 🔴 CRITICAL SECURITY CONFIG ISSUES FOUND  
**Action Required:** Fix session management + remove duplicates  
**Impact:** Some endpoints working inconsistently

