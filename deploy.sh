#!/bin/bash
# VetSoft Production Deploy Script
# Usage: ./deploy.sh [server-ip]
#
# Prerequisites:
#   1. SSH key configured for server
#   2. Docker + Docker Compose installed on server
#   3. .env file with secrets on server at ~/vetsoft/.env

set -euo pipefail

SERVER="${1:-}"
REMOTE_DIR="~/vetsoft"

if [ -z "$SERVER" ]; then
    echo "Usage: ./deploy.sh user@server-ip"
    echo ""
    echo "First time setup:"
    echo "  1. SSH into server: ssh user@server-ip"
    echo "  2. Install Docker: curl -fsSL https://get.docker.com | sh"
    echo "  3. Create dir:     mkdir -p ~/vetsoft/backups"
    echo "  4. Create .env:    cp .env.example ~/vetsoft/.env && nano ~/vetsoft/.env"
    echo "  5. Run this script: ./deploy.sh user@server-ip"
    exit 1
fi

echo "==> Deploying VetSoft to $SERVER..."

# Sync project files (excluding secrets, node_modules, etc.)
echo "==> Syncing files..."
rsync -avz --delete \
    --exclude='.git' \
    --exclude='node_modules' \
    --exclude='.shadow-cljs' \
    --exclude='target' \
    --exclude='.env' \
    --exclude='backups' \
    --exclude='specs' \
    --exclude='.specify' \
    --exclude='.claude' \
    ./ "$SERVER:$REMOTE_DIR/"

# Build and start on server
echo "==> Building and starting services..."
ssh "$SERVER" "cd $REMOTE_DIR && docker compose -f docker-compose.prod.yml up --build -d"

# Check health
echo "==> Waiting for health check..."
sleep 15
ssh "$SERVER" "curl -sf http://localhost:8080/health || echo 'Health check failed!'"

echo "==> Deploy complete!"
echo "    Site: https://vetsoft.ru"
echo "    Admin: https://vetsoft.ru/admin/"
echo "    Health: https://vetsoft.ru/health"
echo ""
echo "    DB access via SSH tunnel:"
echo "    ssh -L 5433:localhost:5432 $SERVER"
echo "    Then connect to localhost:5433 in DBeaver/DataGrip"
