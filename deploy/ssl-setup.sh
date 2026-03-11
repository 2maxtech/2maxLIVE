#!/bin/bash
set -e

# ============================================================
# SSL Setup with Let's Encrypt
# Usage: ./ssl-setup.sh your-domain.com
# ============================================================

DOMAIN=${1:?Usage: ./ssl-setup.sh your-domain.com}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=== Setting up SSL for $DOMAIN ==="

# Get certificate
docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" run --rm certbot \
    certonly --webroot --webroot-path=/var/www/certbot \
    --email admin@$DOMAIN --agree-tos --no-eff-email \
    -d $DOMAIN

# Update nginx config
CONF="$SCRIPT_DIR/nginx/conf.d/default.conf"
cat > "$CONF" << NGINXEOF
# HTTP — redirect to HTTPS
server {
    listen 80;
    server_name $DOMAIN;

    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

# HTTPS
server {
    listen 443 ssl;
    server_name $DOMAIN;

    ssl_certificate /etc/letsencrypt/live/$DOMAIN/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/$DOMAIN/privkey.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://web:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }
}
NGINXEOF

# Reload nginx
docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" exec nginx nginx -s reload

echo ""
echo "=== SSL configured! ==="
echo "Your app is now live at: https://$DOMAIN"
echo ""
