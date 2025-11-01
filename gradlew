#!/usr/bin/env sh
# Auto-downloading Gradle Wrapper (for GitHub Actions / Codespaces)
set -e
DIR=$(cd "$(dirname "$0")"; pwd)
WRAPPER_DIR="$DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
WRAPPER_PROPS="$WRAPPER_DIR/gradle-wrapper.properties"

# Create wrapper properties if missing
if [ ! -f "$WRAPPER_PROPS" ]; then
  mkdir -p "$WRAPPER_DIR"
  cat <<EOF > "$WRAPPER_PROPS"
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.2-bin.zip
EOF
fi

# Download official Gradle wrapper jar if missing
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "Downloading Gradle wrapper JAR..."
  curl -sL -o "$WRAPPER_JAR" https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar
fi

# Launch Gradle
exec java -jar "$WRAPPER_JAR" "$@"
