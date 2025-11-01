#!/usr/bin/env sh
# Fixed Gradle Wrapper Downloader — works on GitHub Actions & Codespaces
set -e

DIR=$(cd "$(dirname "$0")"; pwd)
WRAPPER_DIR="$DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
WRAPPER_PROPS="$WRAPPER_DIR/gradle-wrapper.properties"

# Ensure wrapper directory exists
mkdir -p "$WRAPPER_DIR"

# Create properties file if missing
if [ ! -f "$WRAPPER_PROPS" ]; then
  cat <<EOF > "$WRAPPER_PROPS"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2-bin.zip
EOF
fi

# ✅ Download the official gradle-wrapper.jar from Gradle’s repo if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading official Gradle wrapper jar..."
  curl -sL -o "$WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
fi

# Run Gradle via wrapper jar
exec java -jar "$WRAPPER_JAR" "$@"
