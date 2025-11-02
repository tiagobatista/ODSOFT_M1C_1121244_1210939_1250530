# MEI-odsoft-2025-2026-1191577-1210939-1250530

odsoft 2025/2026 repo of students:



1210939 - Nuno De Brito Castro Moura De Oliveira

1250530 - Josimar Nascimento Lopes Bazilio

---

## 📚 Documentation

### Configuration & Setup
- **[Database Switching Guide](random/DATABASE_SWITCHING_GUIDE.md)** - Complete step-by-step guide for switching between database providers
- **[Quick Reference](random/DATABASE_SWITCHING_QUICK_REF.md)** - One-page cheat sheet for database switching
- **[Persistence Configuration](random/PERSISTENCE_CONFIG.md)** - Detailed persistence strategy configuration
- **[Setup Complete](random/SETUP_COMPLETE.md)** - Initial setup and configuration overview

### Testing
- **[Configuration Tests Summary](random/CONFIGURATION_TESTS_SUMMARY.md)** - Complete guide to configuration tests (22 tests)
- **[Test Commands](random/PERSISTENCE_TESTS_COMMANDS.md)** - All available test commands
- **[Quick Test Reference](random/PERSISTENCE_TESTS_QUICK_REF.md)** - Quick test execution guide

### Redis Implementation
- **[Redis Status Assessment](random/REDIS_STATUS_ASSESSMENT.md)** - Current Redis implementation status
- **[Redis Quick Reference](random/REDIS_QUICK_REFERENCE.md)** - Redis configuration and usage guide

### Architecture & Design
- **[ADD Report](arcsoft-2025/P1/Documentation/Report/report-p1.md)** - Architecture Decision Document
- **[Implementation Summary](random/IMPLEMENTATION_SUMMARY.md)** - Technical implementation details

---

## 🚀 Quick Start

### Current Configuration (SQL + Redis)

The application is configured with:
- **Database**: H2 (in-memory SQL)
- **Caching**: Redis (disabled by default)
- **Profile**: `sql-redis`

### Running the Application

```bash
# Start application with default configuration
mvn spring-boot:run

# Access the application
http://localhost:8080

# Access H2 Console (development)
http://localhost:8080/h2-console
```

### Switching Database Providers

**See [DATABASE_SWITCHING_GUIDE.md](random/DATABASE_SWITCHING_GUIDE.md) for detailed instructions.**

Quick switch:
```properties
# Edit src/main/resources/application.properties

# For MongoDB + Redis
spring.profiles.active=mongodb-redis,bootstrap
persistence.strategy=mongodb-redis

# For ElasticSearch  
spring.profiles.active=elasticsearch,bootstrap
persistence.strategy=elasticsearch
```

### Running Tests

```bash
# Run all configuration tests
mvn test -Dtest="*Configuration*Test"

# Expected: 22 tests pass
```

---

## 📋 Current Implementation Status

| Feature | Status | Details |
|---------|--------|---------|
| **SQL + Redis** | ✅ Fully Implemented | H2 database with Redis caching infrastructure |
| **Configuration Tests** | ✅ 22/22 Passing | Profile-based loading validated |
| **Redis Caching** | ✅ Infrastructure Ready | Disabled by default, can be enabled via config |
| **MongoDB + Redis** | 🚧 Infrastructure Ready | Configuration ready, repositories TODO |
| **ElasticSearch** | 🚧 Infrastructure Ready | Configuration ready, repositories TODO |

---

## 🔧 Key Configuration Files

- `src/main/resources/application.properties` - Main configuration
- `src/main/resources/application-sql-redis.properties` - SQL + Redis settings
- `src/main/resources/application-mongodb-redis.properties` - MongoDB + Redis settings
- `src/main/resources/application-elasticsearch.properties` - ElasticSearch settings

---

## 📖 Architecture Highlights

This project demonstrates **configuration-driven runtime behavior**:

- ✅ Switch database providers by changing configuration (no code changes!)
- ✅ Enable/disable caching at setup time
- ✅ Profile-based conditional bean loading
- ✅ All alternatives defined during configuration (setup time)
- ✅ Configuration changes directly impact runtime behavior

**Complies with ADD requirement**: *"Alternatives must be defined during configuration (setup time), which directly impacts runtime behavior"*

---

## 🧪 Test Evidence

All 22 configuration tests pass, validating:
- ✅ Profile activation works correctly
- ✅ Correct beans are loaded per profile
- ✅ Alternative strategy beans are excluded
- ✅ Configuration properties bind correctly
- ✅ Repository implementations match active profile

```bash
mvn test -Dtest="*Configuration*Test"
# Result: Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
```

---

## 📞 Support & Troubleshooting

- **Port 6379 in use?** See [Redis Quick Reference](random/REDIS_QUICK_REFERENCE.md)
- **Profile not loading?** See [Database Switching Guide](random/DATABASE_SWITCHING_GUIDE.md)
- **Tests failing?** See [Configuration Tests Summary](random/CONFIGURATION_TESTS_SUMMARY.md)
- **General issues?** Check [Troubleshooting section](random/DATABASE_SWITCHING_GUIDE.md#troubleshooting)

---

**Last Updated**: 2025-10-26  
**Version**: 1.0.0-SNAPSHOT

