# Database Switching Quick Reference

## One-Page Cheat Sheet

### 🎯 Quick Switch Commands

```properties
# SQL + Redis (Default)
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis

# MongoDB + Redis
spring.profiles.active=mongodb-redis,bootstrap
persistence.strategy=mongodb-redis

# ElasticSearch
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

---

## Configuration Matrix

| Property | SQL+Redis | MongoDB+Redis | ElasticSearch |
|----------|-----------|---------------|---------------|
| **Profile** | `sql-redis` | `mongodb-redis` | `elasticsearch` |
| **Primary DB** | H2/PostgreSQL | MongoDB | ElasticSearch |
| **Caching** | Redis | Redis | Built-in |
| **Port (DB)** | N/A (embedded H2) | 27017 | 9200 |
| **Port (Cache)** | 6379 | 6379 | N/A |
| **Status** | ✅ Working | 🚧 Infrastructure | 🚧 Infrastructure |

---

## What Changes at Runtime

### When `spring.profiles.active=sql-redis`
- ✅ Loads: DataSource, EntityManagerFactory, JPA repositories
- ✅ Loads: RedisConfig (if caching enabled)
- ❌ Excludes: MongoDB, ElasticSearch beans

### When `spring.profiles.active=mongodb-redis`
- ✅ Loads: MongoClient, MongoTemplate, MongoDB repositories
- ✅ Loads: RedisConfig (if caching enabled)
- ❌ Excludes: JPA, ElasticSearch beans

### When `spring.profiles.active=elasticsearch`
- ✅ Loads: ElasticsearchClient, ElasticSearch repositories
- ❌ Excludes: JPA, MongoDB, Redis beans

---

## Caching Control

```properties
# Enable caching (Redis starts)
persistence.caching-enabled=true

# Disable caching (direct DB access)
persistence.caching-enabled=false
```

---

## Files to Edit

| Action | File | Line |
|--------|------|------|
| **Switch strategy** | `application.properties` | Line 8-9 |
| **Enable caching** | `application.properties` | Line 15 |
| **Configure SQL** | `application-sql-redis.properties` | Various |
| **Configure MongoDB** | `application-mongodb-redis.properties` | Various |
| **Configure ES** | `application-elasticsearch.properties` | Various |

---

## Verification Commands

```bash
# 1. Start application
mvn spring-boot:run

# 2. Check active profile in logs
# Look for: "The following profiles are active: ..."

# 3. Test endpoint
curl -u admin@mail.com:AdminPassword1 http://localhost:8080/api/books

# 4. Run configuration tests
mvn test -Dtest="*Configuration*Test"
```

---

## Common Issues & Fixes

| Problem | Cause | Solution |
|---------|-------|----------|
| Port 6379 in use | Redis already running | `taskkill /F /IM redis-server.exe` |
| Wrong beans loaded | Wrong profile | Check `spring.profiles.active` |
| Cache not working | Caching disabled | Set `persistence.caching-enabled=true` |
| App won't start | Profile mismatch | Ensure profile name is exact |

---

## Test Results Matrix

| Test | SQL+Redis | MongoDB+Redis | ElasticSearch |
|------|-----------|---------------|---------------|
| Profile activation | ✅ Pass (9 tests) | 🚧 TODO | 🚧 TODO |
| Config properties | ✅ Pass (6 tests) | 🚧 TODO | 🚧 TODO |
| Repository loading | ✅ Pass (7 tests) | 🚧 TODO | 🚧 TODO |
| **Total** | **22/22 ✅** | **0 🚧** | **0 🚧** |

---

## Environment Variables (Production)

```bash
# Linux/Mac
export SPRING_PROFILES_ACTIVE=sql-redis,bootstrap
export PERSISTENCE_STRATEGY=sql-redis
export PERSISTENCE_CACHING_ENABLED=true

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="sql-redis,bootstrap"
$env:PERSISTENCE_STRATEGY="sql-redis"
$env:PERSISTENCE_CACHING_ENABLED="true"
```

---

## Cache TTL Defaults

| Entity | TTL | Reason |
|--------|-----|--------|
| ISBN | 24 hours | Rarely changes |
| Books | 1 hour | Occasional updates |
| Authors | 1 hour | Relatively stable |
| Readers | 1 hour | Moderate updates |
| Lendings | 15 minutes | Frequent changes |

---

## Full Documentation

- **Detailed Guide**: `DATABASE_SWITCHING_GUIDE.md`
- **Config Details**: `PERSISTENCE_CONFIG.md`
- **Test Summary**: `CONFIGURATION_TESTS_SUMMARY.md`
- **Redis Status**: `REDIS_STATUS_ASSESSMENT.md`

---

**⚠️ Remember:**
1. Stop app before switching
2. Only change configuration (never code!)
3. Restart after changes
4. Verify with logs and tests

**Last Updated**: 2025-10-26

