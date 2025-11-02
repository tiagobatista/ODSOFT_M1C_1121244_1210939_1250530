# Elasticsearch Restart Script for Windows 11
# Run this with: powershell -ExecutionPolicy Bypass -File restart-elasticsearch.ps1

Write-Host "========================================"
Write-Host "ELASTICSEARCH - RESTART AFTER BUG FIXES"
Write-Host "Date: 2025-10-28"
Write-Host "========================================"
Write-Host ""

Write-Host "[1/7] Stopping Java processes..."
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Write-Host "Done."
Write-Host ""

Write-Host "[2/7] Stopping Elasticsearch..."
docker stop elasticsearch 2>$null
Write-Host "Done."
Write-Host ""

Write-Host "[3/7] Removing container..."
docker rm elasticsearch 2>$null
Write-Host "Done."
Write-Host ""

Write-Host "[4/7] Cleaning volumes..."
docker volume prune -f
Write-Host "Done."
Write-Host ""

Write-Host "[5/7] Starting fresh Elasticsearch..."
docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" docker.elastic.co/elasticsearch/elasticsearch:8.11.0
Write-Host "Done."
Write-Host ""

Write-Host "[6/7] Waiting 15 seconds..."
Start-Sleep -Seconds 15
Write-Host "Done."
Write-Host ""

Write-Host "[7/7] Starting application..."
Write-Host ""
Write-Host "========================================"
Write-Host "WATCH FOR THESE LOG MESSAGES:"
Write-Host "========================================"
Write-Host "- '✓ Created 4 users'"
Write-Host "- '✓ Created 7 genres'"
Write-Host "- '✓ Created 6 authors'"
Write-Host "- '✓ Created 6 books'"
Write-Host "- '✅ Elasticsearch bootstrapping completed!'"
Write-Host ""
Write-Host "NO BCrypt warnings should appear!"
Write-Host "========================================"
Write-Host ""

mvn spring-boot:run

