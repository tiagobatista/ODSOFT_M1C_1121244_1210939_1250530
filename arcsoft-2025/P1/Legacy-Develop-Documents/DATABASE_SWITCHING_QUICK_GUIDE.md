# Quick Database Switching Guide

## Available Database Strategies

The application supports three persistence strategies that can be switched at **configuration time**:

1. **SQL + Redis** (Default) - H2/PostgreSQL/MySQL with Redis caching
2. **MongoDB + Redis** - MongoDB with Redis caching  
3. **Elasticsearch** - Elasticsearch search engine

## How to Switch

### Method 1: Edit application.properties

Edit `src/main/resources/application.properties`:

```properties
# For SQL + Redis (default)
spring.profiles.active=sql-redis,bootstrap
persistence.strategy=sql-redis

# For MongoDB + Redis
spring.profiles.active=mongodb-redis,bootstrap
persistence.strategy=mongodb-redis

# For Elasticsearch
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

**After changing**: Restart the application

### Method 2: Command Line (Maven)

```bash
# SQL + Redis
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap

# MongoDB + Redis
mvn spring-boot:run -Dspring-boot.run.profiles=mongodb-redis,bootstrap

# Elasticsearch
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### Method 3: Environment Variable

**Windows CMD:**
```cmd
set SPRING_PROFILES_ACTIVE=sql-redis,bootstrap
java -jar target\psoft-g1-0.0.1-SNAPSHOT.jar
```

**Windows PowerShell:**
```powershell
$env:SPRING_PROFILES_ACTIVE="sql-redis,bootstrap"
java -jar target\psoft-g1-0.0.1-SNAPSHOT.jar
```

**Linux/Mac:**
```bash
export SPRING_PROFILES_ACTIVE=sql-redis,bootstrap
java -jar target/psoft-g1-0.0.1-SNAPSHOT.jar
```

### Method 4: IDE Configuration

**IntelliJ IDEA:**
1. Run → Edit Configurations
2. Select your Spring Boot configuration
3. In "Active Profiles" field, enter: `sql-redis,bootstrap` (or other profile)
4. Apply and Run

**Eclipse/STS:**
1. Run → Run Configurations
2. Select your Spring Boot App
3. Arguments tab → VM arguments: `-Dspring.profiles.active=sql-redis,bootstrap`
4. Apply and Run

## Prerequisites by Strategy

### SQL + Redis
```bash
# Redis (optional - app works without it if caching disabled)
docker run -d --name redis -p 6379:6379 redis:latest

# H2 - No setup needed (in-memory database)
# PostgreSQL - docker run -d --name postgres -p 5432:5432 -e POSTGRES_PASSWORD=password postgres
# MySQL - docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password mysql
```

**Status:** ✅ Fully Implemented and Tested

### MongoDB + Redis
```bash
# MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Redis (optional)
docker run -d --name redis -p 6379:6379 redis:latest
```

**Status:** 🚧 Infrastructure Ready, Repository Implementations Pending

### Elasticsearch
```bash
# Elasticsearch
docker run -d --name elasticsearch \
  -p 9200:9200 \
  -e "discovery.type=single-node" \
  docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

**Status:** ✅ Fully Implemented, Not Yet Tested

## Verification

After switching profiles, verify the correct strategy is active:

### Check Application Startup Logs
```
2025-10-26 ... INFO ... : The following profiles are active: "sql-redis", "bootstrap"
2025-10-26 ... INFO ... : Bootstrapping Spring Data JPA repositories in DEFAULT mode
```

OR

```
2025-10-26 ... INFO ... : The following profiles are active: "elasticsearch", "bootstrap"
2025-10-26 ... INFO ... : Bootstrapping Spring Data Elasticsearch repositories
```

### Check with Endpoint
```bash
# Should work with any active profile
curl http://localhost:8080/api/books/top5 -u admin@gmail.com:AdminPwd1
```

### Check Configuration Bean
The application logs will show which `persistence.strategy` is active.

## Configuration Files

| Strategy | Profile Name | Config File |
|----------|--------------|-------------|
| SQL + Redis | `sql-redis` | `application-sql-redis.properties` |
| MongoDB + Redis | `mongodb-redis` | `application-mongodb-redis.properties` |
| Elasticsearch | `elasticsearch` | `application-elasticsearch.properties` |

## What Changes at Runtime?

When you switch profiles, the following changes automatically:

| Component | SQL + Redis | MongoDB + Redis | Elasticsearch |
|-----------|-------------|-----------------|---------------|
| **Entity Models** | JPA Entities (`@Entity`) | MongoDB Documents (`@Document`) | ES Documents (`@Document`) |
| **Repositories** | JpaRepository | MongoRepository | ElasticsearchRepository |
| **Repository Impls** | SQL implementations | MongoDB implementations | ES implementations |
| **Database Connection** | JDBC DataSource | MongoClient | ElasticsearchClient |
| **Caching** | Redis (if enabled) | Redis (if enabled) | N/A |
| **Configuration** | JpaConfig, RedisConfig | MongoConfig, RedisConfig | ElasticsearchConfig |

**Domain Models (`Book`, `Author`, `Genre`, etc.) DO NOT CHANGE** - they remain database-agnostic!

## Caching Control

Caching can be enabled/disabled independently:

**In `application.properties` or profile-specific properties:**
```properties
# Enable caching (for sql-redis and mongodb-redis)
persistence.caching-enabled=true

# Disable caching
persistence.caching-enabled=false
```

**Note:** Caching is only applicable for `sql-redis` and `mongodb-redis` profiles. Elasticsearch profile ignores this setting.

## Common Issues

### Issue: "Port 8080 already in use"
**Solution:** Stop the running instance first
```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill -f spring-boot
```

### Issue: "Failed to start embedded Redis"
**Solution:** 
1. Check if port 6379 is available
2. Or disable caching: `persistence.caching-enabled=false`
3. Or use external Redis

### Issue: "Connection refused" (MongoDB/Elasticsearch)
**Solution:** Ensure the database server is running on the expected port

### Issue: "Wrong profile active"
**Solution:** 
1. Check `application.properties` - `spring.profiles.active` is set correctly
2. Clear any environment variables: `unset SPRING_PROFILES_ACTIVE`
3. Check IDE run configuration

### Issue: "Multiple profiles active"
**Solution:** The app supports multiple profiles. Always include `bootstrap` profile for data initialization:
```properties
spring.profiles.active=sql-redis,bootstrap
```

## Profile-Based Bean Loading

The application uses `@Profile` annotations to conditionally load beans:

```java
@Profile("sql-redis")        // Only loaded with sql-redis profile
@Profile("mongodb-redis")    // Only loaded with mongodb-redis profile  
@Profile("elasticsearch")    // Only loaded with elasticsearch profile
@Primary                     // Marks as primary implementation when active
```

This ensures:
- ✅ No bean conflicts
- ✅ Only relevant beans are loaded
- ✅ Clean separation of concerns
- ✅ Easy to add new persistence strategies

## Testing Different Strategies

To test that switching works correctly:

```bash
# 1. Test SQL + Redis
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap
# Make some API calls
# Stop the app (Ctrl+C)

# 2. Test Elasticsearch (requires ES running)
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
# Make some API calls
# Stop the app (Ctrl+C)

# 3. Data is NOT shared between strategies (different databases)
```

## Architecture Benefit

This design demonstrates the **Strategy Pattern** and **Dependency Inversion Principle**:

- **High-level modules** (Services, Controllers) depend on **abstractions** (Repository interfaces)
- **Low-level modules** (JPA, MongoDB, ES implementations) implement the abstractions
- **Configuration** determines which implementation is used at runtime
- **No code changes needed** to switch databases

## Quick Commands

```bash
# Build
mvn clean package -DskipTests

# Run with SQL
mvn spring-boot:run

# Run with Elasticsearch
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap

# Check which profile is active
curl http://localhost:8080/actuator/env | grep "activeProfiles"
```

## Summary

✅ **Configuration-time selection**: Set profile in properties or command line  
✅ **Runtime impact**: Different beans loaded based on profile  
✅ **No code changes**: Just configuration  
✅ **Clean architecture**: Domain logic independent of persistence  
✅ **Easy testing**: Switch profiles to test different databases  
✅ **Production ready**: Use environment variables for deployment  

**Default Profile:** `sql-redis` (safest, requires no external setup with H2)  
**Recommended for Development:** `sql-redis` with H2 in-memory  
**Recommended for Production:** `sql-redis` with PostgreSQL or MySQL  
**For Search-Heavy Workloads:** `elasticsearch`  
**For Document-Heavy Workloads:** `mongodb-redis`

