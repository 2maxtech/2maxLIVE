#!/bin/bash
set -e

# ============================================================
# 2maX LIVE — Hetzner Cloud Server Setup Script
# Run as root on a fresh Ubuntu 24.04 server
# ============================================================

echo "=== 2maX LIVE Server Setup ==="

# --- 1. System updates ---
echo "[1/6] Updating system..."
apt update && apt upgrade -y
apt install -y curl git ufw

# --- 2. Install Docker ---
echo "[2/6] Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
fi

# --- 3. Firewall ---
echo "[3/6] Configuring firewall..."
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable

# --- 4. Clone repo ---
echo "[4/6] Cloning repository..."
INSTALL_DIR="/opt/2maxlive"

if [ ! -d "$INSTALL_DIR" ]; then
    git clone -b feature/cloud-deployment https://github.com/2maxtech/2maxLIVE.git "$INSTALL_DIR"
else
    cd "$INSTALL_DIR" && git pull
fi

# --- 5. Configure environment ---
echo "[5/6] Setting up environment..."
cd "$INSTALL_DIR/deploy"

if [ ! -f .env ]; then
    cp .env.example .env

    # Generate secrets
    DB_PASS=$(openssl rand -base64 16 | tr -d '=/+' | head -c 24)
    JWT=$(openssl rand -base64 32)
    ADMIN_PASS=$(openssl rand -base64 12 | tr -d '=/+' | head -c 16)

    sed -i "s|CHANGE_ME_STRONG_PASSWORD|$DB_PASS|" .env
    sed -i "s|CHANGE_ME_GENERATE_WITH_openssl_rand_base64_32|$JWT|" .env
    sed -i "s|CHANGE_ME_STRONG_PASSWORD|$ADMIN_PASS|" .env

    echo ""
    echo "================================================"
    echo "  Generated credentials (save these!):"
    echo "  DB Password:    $DB_PASS"
    echo "  JWT Secret:     $JWT"
    echo "  Admin Password: $ADMIN_PASS"
    echo "================================================"
    echo ""
    echo "Edit .env to set your DOMAIN and ADMIN_EMAIL:"
    echo "  nano $INSTALL_DIR/deploy/.env"
    echo ""
fi

# --- 6. Deploy ---
echo "[6/6] Starting services..."
docker compose -f docker-compose.prod.yml up -d --build

echo ""
echo "=== Deployment complete! ==="
echo ""
echo "App running at: http://$(curl -s ifconfig.me):80"
echo ""
echo "Next steps:"
echo "  1. Point your domain DNS to this server IP"
echo "  2. Run: $INSTALL_DIR/deploy/ssl-setup.sh your-domain.com"
echo "  3. Log in at https://your-domain.com with your admin credentials"
echo ""
