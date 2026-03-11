#!/bin/bash
set -e

# ============================================================
# Database Backup Script
# Usage: ./backup.sh
# Add to cron: 0 3 * * * /opt/2maxlive/deploy/backup.sh
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKUP_DIR="$SCRIPT_DIR/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

source "$SCRIPT_DIR/.env"

mkdir -p "$BACKUP_DIR"

echo "Backing up database..."
docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" exec -T db \
    pg_dump -U "${DB_USER:-twomaxlive}" "${DB_NAME:-twomaxlive}" \
    | gzip > "$BACKUP_DIR/db_$TIMESTAMP.sql.gz"

# Keep only last 30 backups
ls -t "$BACKUP_DIR"/db_*.sql.gz | tail -n +31 | xargs -r rm

echo "Backup saved: $BACKUP_DIR/db_$TIMESTAMP.sql.gz"
