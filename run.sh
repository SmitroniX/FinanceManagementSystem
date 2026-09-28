#!/usr/bin/env bash
# ==============================================================================
# FinVantage - Personal & Business Finance Management System
# Launcher Script (Linux / macOS)
# ==============================================================================

set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "--------------------------------------------------"
echo " Starting FinVantage (Java Swing + Oracle Database)"
echo "--------------------------------------------------"

if [ -f "target/finvantage-swing-oracle-1.0.0-jar-with-dependencies.jar" ]; then
    java -jar target/finvantage-swing-oracle-1.0.0-jar-with-dependencies.jar "$@"
else
    echo "[!] Fat JAR not found. Compiling and launching via Maven..."
    mvn clean compile exec:java -Dexec.mainClass="com.finvantage.Main"
fi
