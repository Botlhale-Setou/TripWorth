#!/usr/bin/env sh
# Gradle wrapper launcher that fetches official gradle-wrapper.jar if missing
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_DIR="$DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
WRAPPER_PROPS="$WRAPPER_DIR/gradle-wrapper.properties"

mkdir -p "$WRAPPER_DIR"

# Ensure properties exist (should already be in repo)
if [ ! -f "$WRAPPER_PROPS" ]; then
  cat > "$WRAPPER_PROPS" <<'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
EOF
fi

# Download the official wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading official Gradle wrapper jar..."
  curl -sL -o "$WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar
fi

exec java -jar "$WRAPPER_JAR" "$@"
