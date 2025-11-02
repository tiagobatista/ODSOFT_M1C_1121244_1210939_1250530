@echo off
REM Set console to UTF-8 encoding to display emojis correctly
chcp 65001 >nul

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

echo NOTE: Bootstrap data creation may take 1-2 minutes
echo Watch for these log indicators:
echo   [RED X EMOJI] CACHE MISS - Reading from database
echo   [BOOK EMOJI] CACHE HIT - Found in Redis cache
echo   [DISK EMOJI] Saved to SQL database
echo   [REFRESH EMOJI] Updated Redis cache
echo.

echo Starting application...
echo.

call mvn spring-boot:run

