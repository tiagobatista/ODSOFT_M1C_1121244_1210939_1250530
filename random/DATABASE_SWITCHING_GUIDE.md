# Database Provider Switching Guide

## Overview

This guide provides **step-by-step instructions** for switching between database providers at configuration time. The application supports three persistence strategies, and switching between them requires only configuration changes—**no code modifications needed**.

---

## Quick Reference

| Strategy | Profile | Primary Database | Caching | Status |
|----------|---------|-----------------|---------|--------|
| **SQL + Redis** | `sql-redis` | H2/PostgreSQL/MySQL | Redis | ✅ Fully Implemented |
| **MongoDB + Redis** | `mongodb-redis` | MongoDB | Redis | 🚧 Infrastructure Ready |
| **ElasticSearch** | `elasticsearch` | ElasticSearch | Built-in | 🚧 Infrastructure Ready |

---

## Prerequisites

Before switching database providers:

1. ✅ **Stop the running application** (if running)
2. ✅ **Backup any important data** (if using persistent storage)
3. ✅ **Ensure target database is available** (for external databases)
4. ✅ **Review configuration requirements** for target strategy

---

## Switching Between Database Providers

### **Method 1: Change Application Properties (Recommended)**

This is the primary method for switching database providers at **configuration time**.

#### **Step 1: Edit `application.properties`**

Location: `src/main/resources/application.properties`

```properties
# BEFORE: SQL + Redis (current default)
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
persistence.caching-enabled=false

# AFTER: MongoDB + Redis (example)
spring.profiles.active=mongodb-redis,bootstrap
persistence.strategy=mongodb-redis
persistence.caching-enabled=false
```

#### **Step 2: Save the file**

No compilation needed - Spring Boot reads properties at startup.

#### **Step 3: Restart the application**

```bash
# Stop current application (Ctrl+C if running)

# Start with new configuration
mvn spring-boot:run
```

#### **Step 4: Verify the switch**

Check the startup logs for profile activation:

```
INFO --- [main] pt.psoft.g1.psoftg1.PsoftG1Application   : 
    The following 2 profiles are active: "mongodb-redis", "bootstrap"
```

---

### **Method 2: Command-Line Override (Temporary Testing)**

For temporary testing without modifying files:

```bash
# Run with SQL + Redis
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap

# Run with MongoDB + Redis
mvn spring-boot:run -Dspring-boot.run.profiles=mongodb-redis,bootstrap

# Run with ElasticSearch
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

**Note:** This doesn't change the property files, only the current execution.

---

### **Method 3: Environment Variables (Production)**

For production deployments:

```bash
# Linux/Mac
export SPRING_PROFILES_ACTIVE=mongodb-redis,bootstrap
export PERSISTENCE_STRATEGY=mongodb-redis
java -jar psoft-g1.jar

# Windows (cmd)
set SPRING_PROFILES_ACTIVE=mongodb-redis,bootstrap
set PERSISTENCE_STRATEGY=mongodb-redis
java -jar psoft-g1.jar

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="mongodb-redis,bootstrap"
$env:PERSISTENCE_STRATEGY="mongodb-redis"
java -jar psoft-g1.jar
```

---

## Configuration Details by Strategy

### **Strategy 1: SQL + Redis** (Default)

#### **Configuration File:** `application-sql-redis.properties`

```properties
# Database: H2 (development) or PostgreSQL/MySQL (production)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# Redis Caching
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis

# H2 Console (development only)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

#### **What Gets Loaded:**
- ✅ `DataSource` bean (SQL connection)
- ✅ `EntityManagerFactory` (JPA)
- ✅ `TransactionManager` (JPA transactions)
- ✅ `RedisConnectionFactory` (Redis connection)
- ✅ `CacheManager` (Redis cache manager)
- ✅ SQL entity classes (`@Entity` annotated)
- ✅ JPA repositories (`SpringData*Repository`)

#### **What Gets Excluded:**
- ❌ MongoDB beans
- ❌ ElasticSearch beans
- ❌ MongoDB repositories
- ❌ Document-based entities

---

### **Strategy 2: MongoDB + Redis** 🚧

#### **Configuration File:** `application-mongodb-redis.properties`

```properties
# MongoDB Database
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=psoft_library
spring.data.mongodb.authentication-database=admin
# spring.data.mongodb.username=mongouser
# spring.data.mongodb.password=mongopass

# Redis Caching
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis

# MongoDB Auto-index creation
spring.data.mongodb.auto-index-creation=true
```

#### **What Gets Loaded:**
- ✅ `MongoClient` bean
- ✅ `MongoTemplate` bean
- ✅ `RedisConnectionFactory` (Redis connection)
- ✅ `CacheManager` (Redis cache manager)
- ✅ MongoDB document classes (`@Document` annotated)
- ✅ MongoDB repositories

#### **What Gets Excluded:**
- ❌ SQL/JPA beans
- ❌ ElasticSearch beans
- ❌ JPA entities
- ❌ JPA repositories

---

### **Strategy 3: ElasticSearch** 🚧

#### **Configuration File:** `application-elasticsearch.properties`

```properties
# ElasticSearch Configuration
spring.elasticsearch.uris=http://localhost:9200
spring.elasticsearch.username=elastic
spring.elasticsearch.password=elastic
spring.elasticsearch.connection-timeout=10s
spring.elasticsearch.socket-timeout=60s

# No external caching needed (ElasticSearch has built-in caching)
```

#### **What Gets Loaded:**
- ✅ `RestHighLevelClient` bean (ElasticSearch client)
- ✅ `ElasticsearchTemplate` bean
- ✅ ElasticSearch document classes
- ✅ ElasticSearch repositories

#### **What Gets Excluded:**
- ❌ SQL/JPA beans
- ❌ MongoDB beans
- ❌ Redis beans (ElasticSearch uses internal cache)

---

## Enabling/Disabling Redis Caching

Redis caching is available for **SQL** and **MongoDB** strategies (not ElasticSearch).

### **Enable Caching:**

Edit `application.properties`:

```properties
# Enable Redis caching
persistence.caching-enabled=true

# Use embedded Redis (development only)
persistence.use-embedded-redis=true
```

### **Disable Caching:**

```properties
# Disable Redis caching (direct database access)
persistence.caching-enabled=false
```

### **Cache TTL Configuration:**

Customize Time-To-Live for each entity type:

```properties
# Cache TTL in seconds
persistence.cache-ttl.lendings=900    # 15 minutes (changes frequently)
persistence.cache-ttl.books=3600      # 1 hour (relatively stable)
persistence.cache-ttl.authors=3600    # 1 hour
persistence.cache-ttl.readers=3600    # 1 hour  
persistence.cache-ttl.isbn=86400      # 24 hours (rarely changes)
```

---

## Complete Switching Examples

### **Example 1: Development → SQL + Redis (with caching)**

**Before:**
```properties
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
persistence.caching-enabled=false
```

**After:**
```properties
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
persistence.caching-enabled=true        # ← Enable caching
persistence.use-embedded-redis=true     # ← Use embedded Redis
```

**Runtime Impact:**
- ✅ Embedded Redis starts on port 6379
- ✅ All `@Cacheable` methods become active
- ✅ Database queries are cached
- ✅ Response time improves (< 50ms for cached data)

---

### **Example 2: SQL → MongoDB (when implemented)**

**Before:**
```properties
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis
```

**After:**
```properties
spring.profiles.active=mongodb-redis,bootstrap  # ← Change profile
persistence.strategy=mongodb-redis              # ← Change strategy
```

**Runtime Impact:**
- ✅ SQL beans NOT loaded
- ✅ MongoDB beans loaded instead
- ✅ Document-based entities used
- ✅ MongoDB repositories active
- ✅ Same domain logic, different persistence

---

### **Example 3: Production with External Databases**

**Configuration:** `application-mongodb-redis.properties` (production)

```properties
# External MongoDB
spring.data.mongodb.host=mongo-prod.example.com
spring.data.mongodb.port=27017
spring.data.mongodb.database=psoft_library_prod
spring.data.mongodb.username=${MONGO_USER}
spring.data.mongodb.password=${MONGO_PASSWORD}

# External Redis
spring.data.redis.host=redis-prod.example.com
spring.data.redis.port=6379
spring.data.redis.password=${REDIS_PASSWORD}

# Disable embedded Redis in production
persistence.use-embedded-redis=false
persistence.caching-enabled=true
```

**Environment Variables:**
```bash
export MONGO_USER=produser
export MONGO_PASSWORD=securepass123
export REDIS_PASSWORD=redispass456
export SPRING_PROFILES_ACTIVE=mongodb-redis,bootstrap
```

---

## Verification Checklist

After switching database providers, verify:

### ✅ **1. Application Starts Successfully**

Check logs for:
```
INFO --- [main] pt.psoft.g1.psoftg1.PsoftG1Application   : 
    Started PsoftG1Application in X.XXX seconds
```

### ✅ **2. Correct Profile is Active**

Look for:
```
INFO --- [main] pt.psoft.g1.psoftg1.PsoftG1Application   : 
    The following profiles are active: "YOUR-CHOSEN-PROFILE", "bootstrap"
```

### ✅ **3. Correct Beans are Loaded**

**For SQL + Redis:**
```
INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : 
    Tomcat initialized with port 8080
INFO --- [main] com.zaxxer.hikari.HikariDataSource       : 
    HikariPool-1 - Starting...
INFO --- [main] redis.embedded.RedisServer               : 
    Embedded Redis started on port 6379  (if caching enabled)
```

**For MongoDB + Redis:**
```
INFO --- [main] org.mongodb.driver.cluster              : 
    Cluster created with settings {...}
INFO --- [main] redis.embedded.RedisServer               : 
    Embedded Redis started on port 6379
```

### ✅ **4. Database Connection Works**

Test endpoints:
```bash
# SQL + Redis
curl -u admin@mail.com:AdminPassword1 http://localhost:8080/api/books

# MongoDB + Redis (when implemented)
curl -u admin@mail.com:AdminPassword1 http://localhost:8080/api/books

# Should return same data structure regardless of persistence strategy
```

### ✅ **5. Configuration Tests Pass**

Run configuration tests to verify:
```bash
mvn test -Dtest="*Configuration*Test"
```

Expected result:
```
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Troubleshooting

### **Problem: "Port 6379 already in use"**

**Cause:** Another Redis instance is running or port is occupied.

**Solution:**
```bash
# Option 1: Stop existing Redis
# Windows
taskkill /F /IM redis-server.exe

# Linux/Mac
sudo killall redis-server

# Option 2: Disable embedded Redis and use external
persistence.use-embedded-redis=false
persistence.caching-enabled=true
spring.data.redis.host=your-redis-server.com
```

---

### **Problem: "Failed to load ApplicationContext"**

**Cause:** Wrong profile name or missing configuration file.

**Solution:**
1. Check profile name matches one of: `sql-redis`, `mongodb-redis`, `elasticsearch`
2. Verify corresponding `application-{profile}.properties` file exists
3. Check for typos in profile names

---

### **Problem: MongoDB/ElasticSearch not working**

**Cause:** Repository implementations not yet created (infrastructure only).

**Solution:**
These strategies have infrastructure ready but repository implementations are still TODO. Use `sql-redis` for now, which is fully implemented.

---

### **Problem: Caching not working when enabled**

**Cause:** Redis connection failed or wrong configuration.

**Solution:**
1. Check Redis is running:
   ```bash
   redis-cli ping
   # Should return: PONG
   ```

2. Verify configuration:
   ```properties
   persistence.caching-enabled=true
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   ```

3. Check logs for Redis connection errors

---

## Best Practices

### ✅ **DO:**

1. **Always stop the application before changing profiles**
2. **Test configuration changes in development first**
3. **Use environment variables in production** (don't hardcode secrets)
4. **Keep `bootstrap` profile active** for initial data loading
5. **Verify with tests** after switching (`mvn test -Dtest="*Configuration*Test"`)
6. **Document your chosen strategy** in deployment notes

### ❌ **DON'T:**

1. **Don't mix profiles** (e.g., `sql-redis,mongodb-redis` - choose one!)
2. **Don't change profiles while app is running** (requires restart)
3. **Don't use embedded databases in production** (use external)
4. **Don't hardcode passwords** (use environment variables)
5. **Don't skip verification** after switching
6. **Don't change code** - only configuration should change!

---

## Architecture Compliance

### **ADD Requirement:**

> "The previous alternatives must be defined during configuration (setup time), which directly impacts runtime behavior"

### **How This Implementation Complies:**

| Setup Time Action | Runtime Behavior Change |
|-------------------|------------------------|
| Set `spring.profiles.active=sql-redis` | ✅ JPA/Hibernate beans loaded, SQL queries executed |
| Set `spring.profiles.active=mongodb-redis` | ✅ MongoDB beans loaded, document queries executed |
| Set `persistence.caching-enabled=true` | ✅ Redis cache becomes active, queries cached |
| Set `persistence.caching-enabled=false` | ✅ Direct database access, no caching |

**Key Point:** Configuration changes at setup time **directly impact** which beans are loaded and how the application behaves at runtime—**without any code changes**.

---

## Summary

### **To Switch Database Providers:**

1. **Edit** `application.properties`
2. **Change** `spring.profiles.active` to desired strategy
3. **Update** `persistence.strategy` to match
4. **Save** and **restart** the application
5. **Verify** correct profile is active in logs

### **Currently Available:**

- ✅ **SQL + Redis**: Fully functional
- 🚧 **MongoDB + Redis**: Infrastructure ready, repositories TODO
- 🚧 **ElasticSearch**: Infrastructure ready, repositories TODO

### **No Code Changes Required!**

All switching is done purely through configuration—demonstrating true **configuration-driven runtime behavior** as mandated by the ADD.

---

## References

- **Configuration Details**: `PERSISTENCE_CONFIG.md`
- **Configuration Tests**: `CONFIGURATION_TESTS_SUMMARY.md`
- **Redis Implementation**: `REDIS_STATUS_ASSESSMENT.md`
- **ADD Report**: `arcsoft-2025/P1/Documentation/Report/report-p1.md`
- **Test Commands**: `PERSISTENCE_TESTS_COMMANDS.md`

---

**Last Updated**: 2025-10-26  
**Status**: ✅ SQL + Redis fully operational, switching mechanism validated by tests

