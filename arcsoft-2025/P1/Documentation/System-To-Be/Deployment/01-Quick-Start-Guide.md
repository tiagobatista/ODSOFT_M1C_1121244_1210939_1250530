# Library Management System - Quick Start Guide

## Overview

This guide provides step-by-step instructions to run the Library Management System with different persistence strategies.

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** (for Redis and Elasticsearch)
- **Git** (for cloning the repository)

---

## Available Persistence Strategies

The application supports three persistence strategies:

1. **SQL + Redis** (H2/PostgreSQL/MySQL with Redis caching)
2. **MongoDB + Redis** (MongoDB with Redis caching) - *In Development*
3. **Elasticsearch** (Elasticsearch search engine)

---

## Quick Start: SQL + Redis (Recommended)

### Step 1: Start Redis Container

```bash
docker run -d --name redis -p 6379:6379 redis:latest
```

**Verify Redis is running:**
```bash
docker ps
# You should see the redis container running
```

### Step 2: Run the Application

**PowerShell (add --% to stop PowerShell parsing):**
```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

**CMD (no special syntax needed):**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=sql-redis,bootstrap -Dspring-boot.run.arguments=--persistence.use-embedded-redis=false
```

### Step 3: Access the Application

- **Application URL:** http://localhost:8080
- **H2 Console (SQL):** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave empty)

### Step 4: Test with Sample Users

The `bootstrap` profile creates test users automatically:

**Readers:**
- Email: `manuel@gmail.com` | Password: `Manuelino123!`
- Email: `joao@gmail.com` | Password: `Joaozinho!123`

**Librarians:**
- Email: `maria@gmail.com` | Password: `Mariaroberta!123`
- Email: `admin@gmail.com` | Password: `AdminPwd1`

**Test Endpoint (Reader):**

*Windows (CMD):*
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

*Linux / Mac:*
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

**Test Endpoint (Librarian - Top 5 Books):**

*Windows (CMD):*
```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

*Linux / Mac:*
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

---

## Quick Start: Elasticsearch

### Step 1: Start Elasticsearch Container

```bash
docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
```

**Wait for Elasticsearch to be ready (~30 seconds):**

*Windows (CMD):*
```cmd
curl.exe http://localhost:9200
```

*Linux / Mac:*
```bash
curl http://localhost:9200
```

### Step 2: Run the Application

**PowerShell:**
```powershell
mvn --% spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

**CMD:**
```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=elasticsearch,bootstrap
```

### Step 3: Access the Application

- **Application URL:** http://localhost:8080
- **Elasticsearch API:** http://localhost:9200
- **Verify Indices:** http://localhost:9200/_cat/indices?v

### Step 4: Test Endpoints

**List all books:**

*Windows (CMD):*
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

*Linux / Mac:*
```bash
curl -u manuel@gmail.com:Manuelino123! http://localhost:8080/api/books
```

**Top 5 books:**

*Windows (CMD):*
```cmd
curl.exe -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

*Linux / Mac:*
```bash
curl -u maria@gmail.com:Mariaroberta!123 http://localhost:8080/api/books/top5
```

**Search by title:**

*Windows (CMD):*
```cmd
curl.exe -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Spring"
```

*Linux / Mac:*
```bash
curl -u manuel@gmail.com:Manuelino123! "http://localhost:8080/api/books/search?title=Spring"
```

---

## Stopping and Cleaning Up

### Stop the Application

**Option 1: If you have the Maven terminal window open (RECOMMENDED)**
- Press `Ctrl+C` in the terminal where Maven is running
- Wait for the application to shut down gracefully

**Option 2: Force kill the Java process**

*Windows (CMD):*
```cmd
REM Find the process using port 8080
netstat -ano | findstr :8080

REM Kill the process (replace PID with the actual process ID)
taskkill /F /PID <PID>

REM Or kill all Java processes (WARNING: stops ALL Java apps)
taskkill /F /IM java.exe
```

*Linux / Mac:*
```bash
# Find and kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or use pkill
pkill -f spring-boot
```

### Stop and Remove Containers

**Redis:**
```bash
docker stop redis
docker rm redis
```

**Elasticsearch:**
```bash
docker stop elasticsearch
docker rm elasticsearch
```

### Clean Maven Build
```bash
mvn clean
```

---

## Troubleshooting

### Issue: How do I know if the application is running?

**Windows (CMD):**
```cmd
netstat -ano | findstr :8080
```

**Linux / Mac:**
```bash
lsof -i:8080
# Or
netstat -tuln | grep 8080
```

**Expected output if running:**
```
TCP    0.0.0.0:8080           0.0.0.0:0              LISTENING       12345
```

The number at the end (12345) is the Process ID (PID).

### Issue: Port 8080 already in use
**Solution:** Kill the Java process
```bash
# Windows
taskkill /F /IM java.exe

# Linux/Mac
pkill -f spring-boot
```

### Issue: Redis connection refused
**Solution:** Ensure Redis container is running
```bash
docker ps
# If not running:
docker start redis
```

### Issue: Elasticsearch not ready
**Solution:** Wait longer or check logs
```bash
docker logs elasticsearch
```

### Issue: "Port 6379 already in use" when using embedded Redis
**Solution:** Use external Redis (as shown above) with `--persistence.use-embedded-redis=false`

---

## Next Steps

- See **02-Database-Configuration-Guide.md** for detailed persistence strategy configuration
- See **03-Redis-Caching-Guide.md** for Redis caching details
- See **04-Elasticsearch-Guide.md** for Elasticsearch search features
- See **05-API-Testing-Guide.md** for complete API documentation

---

## Summary

✅ **SQL + Redis:** Best for general use, production-ready  
✅ **Elasticsearch:** Best for search-heavy workloads  
🚧 **MongoDB + Redis:** In development  

**Default Strategy:** SQL + Redis with H2 (no external database needed)  
**Recommended for Production:** SQL + Redis with PostgreSQL

