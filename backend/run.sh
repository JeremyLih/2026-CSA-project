#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC_DIR="$ROOT_DIR/src/main/java"
OUT_DIR="$ROOT_DIR/out"
M2_DIR="$HOME/.m2/repository"

set -a
source "$ROOT_DIR/.env"
set +a

GSON_JAR="$M2_DIR/com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
HIKARI_JAR="$M2_DIR/com/zaxxer/HikariCP/5.1.0/HikariCP-5.1.0.jar"
POSTGRES_JAR="$M2_DIR/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar"
SLF4J_JAR="$M2_DIR/org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar"

for jar in "$GSON_JAR" "$HIKARI_JAR" "$POSTGRES_JAR" "$SLF4J_JAR"; do
    if [[ ! -f "$jar" ]]; then
        echo "Missing dependency jar: $jar" >&2
        echo "This backend now depends on local Maven cache jars. Install Maven dependencies or restore the jars." >&2
        exit 1
    fi
done

CLASSPATH="$GSON_JAR:$HIKARI_JAR:$POSTGRES_JAR:$SLF4J_JAR"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac -cp "$CLASSPATH" -d "$OUT_DIR" "$SRC_DIR"/*.java
java -cp "$OUT_DIR:$CLASSPATH" Application
