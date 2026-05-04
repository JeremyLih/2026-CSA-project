#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$ROOT_DIR/src/main/java"
OUT_DIR="$ROOT_DIR/out"
LIB_DIR="$ROOT_DIR/lib"

if [[ ! -f "$ROOT_DIR/.env" ]]; then
    echo "Error: backend/.env not found." >&2
    echo "Create it with DB_USER, DB_PASSWORD, GEMINI_API_KEY (and optionally PORT, GEMINI_MODEL)." >&2
    exit 1
fi

set -a
source "$ROOT_DIR/.env"
set +a

mkdir -p "$LIB_DIR"

download_if_missing() {
    local jar="$1"
    local url="$2"
    if [[ ! -f "$LIB_DIR/$jar" ]]; then
        echo "Downloading $jar..."
        curl -fsSL "$url" -o "$LIB_DIR/$jar"
    fi
}

download_if_missing "gson-2.11.0.jar"         "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
download_if_missing "HikariCP-5.1.0.jar"      "https://repo1.maven.org/maven2/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar"
download_if_missing "slf4j-api-2.0.13.jar"    "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar"
download_if_missing "slf4j-simple-2.0.13.jar" "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar"
download_if_missing "postgresql-42.7.3.jar"   "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar"

case "$OSTYPE" in
    msys*|cygwin*|win32*) SEP=";" ;;
    *) SEP=":" ;;
esac

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac -cp "$LIB_DIR/*" -d "$OUT_DIR" "$SRC_DIR"/*.java
java -cp "$OUT_DIR$SEP$LIB_DIR/*" Main
