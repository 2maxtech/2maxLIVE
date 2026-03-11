#!/bin/bash
set -e

# ============================================================
# SSL Setup with Let's Encrypt
# Usage: ./ssl-setup.sh your-domain.com
# ============================================================

DOMAIN=${1:?Usage: ./ssl-setup.sh your-domain.com}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE="docker compose -f $SCRIPT_DIR/docker-compose.prod.yml"

echo "=== Setting up SSL for $DOMAIN ==="

# Get certificate using a standalone certbot run (not the renewal container)
docker run --rm \
    -v deploy_certbot_certs:/etc/letsencrypt \
    -v deploy_certbot_www:/var/www/certbot \
    certbot/certbot \
    certonly --webroot --webroot-path=/var/www/certbot \
    --email admin@${DOMAIN} --agree-tos --no-eff-email \
    -d ${DOMAIN}

# Check if cert was issued
if [ ! -f "/var/lib/docker/volumes/deploy_certbot_certs/_data/live/$DOMAIN/fullchain.pem" ]; then
    echo "Certificate not found. Checking volume name..."
    # Try alternate volume name pattern
    VOLUMES=$(docker volume ls --format '{{.Name}}' | grep certbot_certs)
    echo "Certbot volumes found: $VOLUMES"

    docker run --rm \
        -v ${VOLUMES}:/etc/letsencrypt \
        -v $(docker volume ls --format '{{.Name}}' | grep certbot_www):/var/www/certbot \
        certbot/certbot \
        certonly --webroot --webroot-path=/var/www/certbot \
        --email admin@${DOMAIN} --agree-tos --no-eff-email \
        -d ${DOMAIN}
fi

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

# Restart nginx to pick up new config and certs
$COMPOSE restart nginx

echo ""
echo "=== SSL configured! ==="
echo "Your app is now live at: https://$DOMAIN"
echo ""
