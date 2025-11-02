# Multi-Database Persistence Configuration

## Overview

This application supports multiple persistence strategies that can be configured at setup time, directly impacting runtime behavior. The architecture follows the principles outlined in the ADD (Attribute-Driven Design) documentation.

## Supported Persistence Strategies

### 1. SQL + Redis (Default) ✅ IMPLEMENTED
- **Profile**: `sql-redis`
- **Database**: H2 (in-memory) for development
- **Caching**: Redis with write-through strategy
- **Status**: Fully implemented and active

### 2. MongoDB + Redis 🚧 PLACEHOLDER
- **Profile**: `mongodb-redis`
- **Database**: MongoDB (document-based)
- **Caching**: Redis with write-through strategy
- **Status**: Configuration placeholder ready for implementation

### 3. ElasticSearch 🚧 PLACEHOLDER
- **Profile**: `elasticsearch`
- **Database**: ElasticSearch (search-optimized)
- **Caching**: Built-in ElasticSearch caching
- **Status**: Configuration placeholder ready for implementation

## Configuration

### Selecting a Persistence Strategy

Edit `src/main/resources/application.properties`:

```properties
# Set active profile to desired persistence strategy
spring.profiles.active=sql-redis,bootstrap

# Configure persistence strategy
persistence.strategy=sql-redis
```

### Available Profiles

- **sql-redis**: SQL database (H2/PostgreSQL/MySQL) + Redis caching
- **mongodb-redis**: MongoDB + Redis caching
- **elasticsearch**: ElasticSearch with built-in caching

### Caching Configuration

```properties
# Enable/disable caching
persistence.caching-enabled=true

# Cache TTL (Time To Live) in seconds for different entity types
persistence.cache-ttl.lendings=900    # 15 minutes
persistence.cache-ttl.books=3600      # 1 hour
persistence.cache-ttl.authors=3600    # 1 hour
persistence.cache-ttl.readers=3600    # 1 hour
persistence.cache-ttl.isbn=86400      # 24 hours
```

## Profile-Specific Configurations

### SQL + Redis Configuration
File: `application-sql-redis.properties`

```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver

# Redis (Embedded for development)
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### MongoDB + Redis Configuration
File: `application-mongodb-redis.properties`

```properties
# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=psoft_library

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### ElasticSearch Configuration
File: `application-elasticsearch.properties`

```properties
# ElasticSearch
spring.elasticsearch.uris=http://localhost:9200
```

## Architecture Components

### Configuration Classes

1. **PersistenceConfig.java**: Main configuration properties class
2. **RedisConfig.java**: Redis cache manager and template configuration
3. **EmbeddedRedisConfig.java**: Embedded Redis for development
4. **JpaConfig.java**: JPA/Hibernate configuration (SQL only)
5. **MongoConfig.java**: MongoDB configuration (placeholder)
6. **ElasticSearchConfig.java**: ElasticSearch configuration (placeholder)

### Profile Activation

Configurations are conditionally loaded based on active profiles:
- `@Profile("sql-redis")` - Only for SQL + Redis strategy
- `@Profile("mongodb-redis")` - Only for MongoDB + Redis strategy
- `@Profile("elasticsearch")` - Only for ElasticSearch strategy

## Development vs Production

### Development (Default)
- Uses embedded H2 database (in-memory)
- Uses embedded Redis server (port 6379)
- Auto-creates database schema
- H2 console enabled at `/h2-console`

### Production
Configure external databases in profile-specific properties:

```properties
# Disable embedded Redis
persistence.use-embedded-redis=false

# Configure external Redis
spring.data.redis.host=redis.example.com
spring.data.redis.port=6379
spring.data.redis.password=your-password

# Configure external SQL database (e.g., PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/psoft_library
spring.datasource.username=dbuser
spring.datasource.password=dbpassword
```

## Caching Strategy

### Write-Through Caching
- **Reads**: Check cache first (< 50ms response time target)
- **Writes**: Update both database and cache simultaneously
- **Eviction**: TTL-based, varies by entity type

### Cache Hit Rate Targets
- > 80% for frequently accessed data (popular books, active readers)
- Reduces database load significantly
- Improves response times: 50-200ms (DB) → < 50ms (cache)

## Testing Different Strategies

### Switch to MongoDB + Redis (when implemented)
```properties
spring.profiles.active=mongodb-redis,bootstrap
persistence.strategy=mongodb-redis
```

### Switch to ElasticSearch (when implemented)
```properties
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

## Dependencies

All persistence strategy dependencies are included in `pom.xml`:

```xml
<!-- SQL + Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- MongoDB + Redis (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
    <optional>true</optional>
</dependency>

<!-- ElasticSearch (optional) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
    <optional>true</optional>
</dependency>
```

## Implementation Roadmap

### ✅ Phase 1: Infrastructure Setup (COMPLETED)
- [x] Configuration properties system
- [x] Profile-based configuration files
- [x] Redis integration for SQL strategy
- [x] Embedded Redis for development
- [x] Profile-conditional configuration loading

### 🚧 Phase 2: MongoDB Implementation (TODO)
- [ ] Create MongoDB repository implementations
- [ ] Implement document-based data models
- [ ] Configure MongoDB transactions
- [ ] Test MongoDB + Redis integration

### 🚧 Phase 3: ElasticSearch Implementation (TODO)
- [ ] Create ElasticSearch repository implementations
- [ ] Define index mappings
- [ ] Implement search queries
- [ ] Configure ElasticSearch cluster

## Troubleshooting

### Port 6379 already in use
If embedded Redis fails to start:
1. Stop any running Redis instances
2. Or configure external Redis: `persistence.use-embedded-redis=false`

### Profile not loading
Verify `spring.profiles.active` matches one of: `sql-redis`, `mongodb-redis`, `elasticsearch`

### Cache not working
1. Check `persistence.caching-enabled=true`
2. Verify Redis connection in logs
3. Check cache annotations on repository methods

## References

- System-To-Be Documentation: `arcsoft-2025/P1/Documentation/System-To-Be/`
- ADD Report: `arcsoft-2025/P1/Documentation/Report/report-p1.md`
- Logic View: `arcsoft-2025/P1/Documentation/System-To-Be/Views/Logic/`
- Implementation View: `arcsoft-2025/P1/Documentation/System-To-Be/Views/Implementation/`

