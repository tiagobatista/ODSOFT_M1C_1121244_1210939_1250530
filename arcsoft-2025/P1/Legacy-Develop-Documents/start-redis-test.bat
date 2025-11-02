@echo off
echo ========================================
echo SQL + REDIS TEST - Starting Application
echo After Master Merge
echo ========================================
echo.

echo Configuration:
echo - Profile: sql-redis,bootstrap
echo - Strategy: sql-redis
echo - Caching: ENABLED
echo - Redis: Embedded (no Docker needed)
echo.

echo What to expect:
echo - Data will be stored in H2 SQL database
echo - Reads will be cached in embedded Redis
echo - Bootstrap will create same data as Elasticsearch
echo - Second reads should be faster (cache hits)
echo.

echo Starting application...
echo (Watch for bootstrap messages and Redis initialization)
echo.

call mvn spring-boot:run

