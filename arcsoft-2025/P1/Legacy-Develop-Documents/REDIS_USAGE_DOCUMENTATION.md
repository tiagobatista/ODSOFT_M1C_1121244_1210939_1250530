# Redis Configuration Usage Documentation

## Overview

The `RedisConfig` class is a Spring configuration component that sets up Redis as a **caching layer** for the application. It's part of the multi-database persistence strategy implementation and is currently **configured but not actively used** by the application code.

---

## Configuration Architecture

### 1. Profile-Based Activation

RedisConfig is conditionally loaded based on Spring profiles:

```java
@Profile({"sql-redis", "mongodb-redis"})
@ConditionalOnProperty(name = "persistence.caching-enabled", havingValue = "true", matchIfMissing = true)
```

**Active when:**
- Profile is `sql-redis` OR `mongodb-redis`
- Property `persistence.caching-enabled=true` (or not set, as it defaults to true)

**Current Status:**
- Active profile: `sql-redis`
- Caching enabled setting: `false` (in application.properties)
- **Result: RedisConfig beans are NOT being created despite being in the classpath**

### 2. Integration with PersistenceConfig

RedisConfig depends on `PersistenceConfig` for TTL (Time-To-Live) values:

```yaml
persistence:
  strategy: sql-redis
  caching-enabled: false          # Currently disabled
  use-embedded-redis: true        # Use embedded Redis for development
  cache-ttl:
    lendings: 900                 # 15 minutes
    books: 3600                   # 1 hour
    authors: 3600                 # 1 hour
    readers: 3600                 # 1 hour
    isbn: 86400                   # 24 hours
```

---

## What RedisConfig Provides

### 1. ObjectMapper Bean (`redisObjectMapper`)

**Purpose:** Custom Jackson ObjectMapper for Redis serialization with Java 8 date/time support

```java
@Bean
public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();
    mapper.activateDefaultTyping(...);
    return mapper;
}
```

**Features:**
- Handles Java 8 date/time types (LocalDate, LocalDateTime, etc.)
- Preserves type information during JSON serialization
- Used by Redis serializers to convert Java objects to/from JSON

### 2. RedisTemplate Bean

**Purpose:** Low-level Redis operations template

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
```

**Configuration:**
- Key serializer: `StringRedisSerializer` (keys stored as plain strings)
- Value serializer: `GenericJackson2JsonRedisSerializer` (values stored as JSON)
- Supports custom Redis operations beyond caching

**Potential Use Cases:**
- Session storage
- Rate limiting
- Distributed locks
- Custom cache operations
- Temporary data storage

### 3. CacheManager Bean

**Purpose:** Spring Cache abstraction layer for declarative caching

```java
@Bean
public CacheManager cacheManager(RedisConnectionFactory connectionFactory)
```

**Entity-Specific Cache Configurations:**

| Cache Name | TTL      | Purpose                          |
|-----------|----------|----------------------------------|
| lendings  | 15 min   | Active lending records           |
| books     | 1 hour   | Book catalog data                |
| authors   | 1 hour   | Author information               |
| readers   | 1 hour   | Reader profiles                  |
| isbn      | 24 hours | ISBN lookups (rarely changes)    |
| users     | 1 hour   | User account data                |

**Default TTL:** 1 hour (for books - used when cache name not specified)

---

## How Caching Would Be Used (When Enabled)

### Declarative Caching with Annotations

**Current Status:** No caching annotations are present in the codebase

**Would be used with:**

```java
// In a service or repository class
@Cacheable(value = "books", key = "#isbn")
public Optional<Book> findByIsbn(String isbn) {
    // Database query - only executed on cache miss
    return springDataBookRepository.findByIsbn(isbn);
}

@CachePut(value = "books", key = "#book.isbn")
public Book save(Book book) {
    // Updates both database and cache
    return springDataBookRepository.save(book);
}

@CacheEvict(value = "books", key = "#isbn")
public void delete(String isbn) {
    // Removes from both database and cache
    springDataBookRepository.deleteByIsbn(isbn);
}
```

---

## Current Implementation Status

### What's Configured

✅ RedisConfig class with all beans defined  
✅ EmbeddedRedisConfig for development (starts Redis on port 6379)  
✅ Profile-based activation (sql-redis, mongodb-redis)  
✅ Entity-specific TTL settings in PersistenceConfig  
✅ Redis connection properties in application-sql-redis.properties  

### What's NOT Being Used

❌ **Caching is disabled** via `persistence.caching-enabled=false`  
❌ **No @Cacheable annotations** in repository or service classes  
❌ **No direct RedisTemplate usage** in application code  
❌ CacheManager bean not injected anywhere  

### Why It's Not Active

The application.properties explicitly disables caching:

```properties
persistence.caching-enabled=false
```

This means:
1. RedisConfig beans are not created (due to `@ConditionalOnProperty`)
2. Embedded Redis still starts (controlled by separate property)
3. Redis server runs but is unused
4. No performance impact from caching layer

---

## Redis Server Configuration

### Development Mode (Current)

Uses **Embedded Redis** via `EmbeddedRedisConfig`:
- Automatically starts on application boot
- Runs on localhost:6379
- In-memory storage (data lost on restart)
- No external dependencies

**Configuration:**
```properties
persistence.use-embedded-redis=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### Production Mode (Recommended)

Would use **External Redis Server**:
```properties
persistence.use-embedded-redis=false
spring.data.redis.host=your-redis-server.com
spring.data.redis.port=6379
spring.data.redis.password=your-password
spring.data.redis.timeout=60000
```

---

## Caching Strategy Design

### Write-Through Pattern

When caching is enabled, it implements a **write-through** strategy:

1. **Read Operations:**
   - Check cache first (target: <50ms response time)
   - On cache miss: query database + populate cache
   - On cache hit: return cached data immediately

2. **Write Operations:**
   - Update database first
   - Update cache (via @CachePut)
   - Ensures cache-database consistency

3. **Delete Operations:**
   - Delete from database
   - Evict from cache (via @CacheEvict)
   - Prevents stale cache entries

### Performance Targets

| Operation | Without Cache | With Cache |
|-----------|---------------|------------|
| Book lookup by ISBN | ~100-200ms | <50ms |
| Author search | ~150-300ms | <50ms |
| Lending list | ~200-400ms | <50ms |

---

## How to Enable Caching

### Step 1: Update Configuration

```properties
# In application.properties
persistence.caching-enabled=true
```

### Step 2: Add Caching Annotations

Example for BookRepository:

```java
@Profile("sql-redis")
@Component
public class BookRepositoryImpl implements BookRepository {
    
    @Cacheable(value = "books", key = "#isbn")
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return springDataBookRepository.findByIsbn(new IsbnEntity(isbn))
                .map(BookEntity::toBook);
    }
    
    @CachePut(value = "books", key = "#result.isbn")
    @Override
    public Book save(Book book) {
        BookEntity entity = BookEntity.fromBook(book);
        return springDataBookRepository.save(entity).toBook();
    }
    
    @CacheEvict(value = "books", key = "#isbn")
    @Override
    public void delete(String isbn) {
        springDataBookRepository.deleteByIsbn(new IsbnEntity(isbn));
    }
}
```

### Step 3: Restart Application

The CacheManager and RedisTemplate beans will be created automatically.

---

## Architecture Benefits

### Why This Design?

1. **Separation of Concerns:**
   - Persistence layer separate from caching layer
   - Easy to enable/disable caching without code changes

2. **Profile-Based Strategy:**
   - Different caching for SQL vs MongoDB
   - No caching for ElasticSearch (has built-in caching)

3. **Configuration Over Code:**
   - TTL values configurable via properties
   - Cache names centralized in one place
   - Easy to tune per environment

4. **Future-Proof:**
   - Infrastructure ready for when performance optimization needed
   - Can enable caching per entity type
   - Supports gradual rollout

---

## Common Questions

### Q: Why is Redis configured but not used?

**A:** The infrastructure is in place for future performance optimization. Currently disabled because:
- Application handles current load without caching
- Adds complexity during development
- Will be enabled when performance becomes a bottleneck

### Q: Does embedded Redis impact performance?

**A:** When caching is disabled (`persistence.caching-enabled=false`), RedisConfig beans are not created, so there's minimal overhead. The embedded Redis server runs but is not accessed.

### Q: Can I use RedisTemplate directly?

**A:** Yes, when caching is enabled, you can inject `RedisTemplate<String, Object>` into any component for custom Redis operations beyond Spring Cache.

### Q: What happens if Redis is unavailable?

**A:** Currently, application startup would fail. For production, you'd want to:
- Configure fallback behavior
- Use external Redis with high availability
- Add circuit breaker pattern

---

## Related Files

- **Configuration:** `RedisConfig.java`
- **Embedded Redis:** `EmbeddedRedisConfig.java`
- **Persistence Settings:** `PersistenceConfig.java`
- **Profile Properties:** `application-sql-redis.properties`, `application-mongodb-redis.properties`
- **Main Properties:** `application.properties`

---

## Summary

**RedisConfig** provides a **complete but inactive** caching infrastructure:
- ✅ Configured and ready to use
- ✅ Profile-aware and environment-flexible
- ✅ Entity-specific TTL settings
- ❌ Currently disabled via configuration
- ❌ No application code using caching annotations

**To activate:** Set `persistence.caching-enabled=true` and add `@Cacheable` annotations to repository methods.

---

*Document created: 2025-10-26*  
*Application Version: 0.0.1-SNAPSHOT*  
*Active Profile: sql-redis*

