#!/bin/bash

echo "=========================================="
echo "Docker Mirror Setup Guide"
echo "=========================================="
echo ""
echo "[ISSUE] Cannot pull Docker images from Docker Hub"
echo "[SOLUTION] Configure Docker registry mirrors"
echo ""

# Detect OS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "=========================================="
    echo "macOS - Docker Desktop"
    echo "=========================================="
    echo "1. Open Docker Desktop"
    echo "2. Click Docker icon → Preferences"
    echo "3. Go to Docker Engine"
    echo "4. Add the following configuration:"
    echo ""
    echo '{'
    echo '  "registry-mirrors": ['
    echo '    "https://docker.mirrors.ustc.edu.cn",'
    echo '    "https://hub-mirror.c.163.com"'
    echo '  ]'
    echo '}'
    echo ""
    echo "5. Click Apply & Restart"
else
    echo "=========================================="
    echo "Linux - Docker Engine"
    echo "=========================================="
    echo "1. Edit /etc/docker/daemon.json:"
    echo ""
    echo "   sudo nano /etc/docker/daemon.json"
    echo ""
    echo "2. Add the following content:"
    echo ""
    echo '{'
    echo '  "registry-mirrors": ['
    echo '    "https://docker.mirrors.ustc.edu.cn",'
    echo '    "https://hub-mirror.c.163.com"'
    echo '  ]'
    echo '}'
    echo ""
    echo "3. Restart Docker:"
    echo ""
    echo "   sudo systemctl daemon-reload"
    echo "   sudo systemctl restart docker"
fi

echo ""
echo "=========================================="
echo "Verify Configuration"
echo "=========================================="
echo "Run: docker info | grep -A 5 'Registry Mirrors'"
echo ""
echo "=========================================="
echo "[NOTE] After configuration, restart the script:"
echo "  cd scripts"
echo "  ./start.sh"
echo "=========================================="
