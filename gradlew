#!/usr/bin/env sh
# Simplified Gradle Wrapper script
DIR=$(cd "$(dirname "$0")"; pwd)
GRADLE_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$GRADLE_JAR" ]; then
  echo "Downloading Gradle wrapper JAR..."
  mkdir -p "$DIR/gradle/wrapper"
  curl -L -o "$GRADLE_JAR" https://services.gradle.org/distributions/gradle-8.2-bin.zip
  unzip -j "$DIR/gradle/wrapper/gradle-8.2-bin.zip" "gradle-8.2/lib/gradle-launcher-*.jar" -d "$DIR/gradle/wrapper"
  rm "$DIR/gradle/wrapper/gradle-8.2-bin.zip"
fi
java -jar "$GRADLE_JAR" "$@"
