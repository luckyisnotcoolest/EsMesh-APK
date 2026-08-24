#!/usr/bin/env bash

# Resolve application directory
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
WRAPPER_JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
    JAVA_EXE="java"
    if [ -n "$JAVA_HOME" ]; then
        JAVA_EXE="$JAVA_HOME/bin/java"
    fi
    exec "$JAVA_EXE" -Dorg.gradle.appname=gradlew -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Error: Neither gradle-wrapper.jar nor system gradle executable was found in PATH." >&2
    exit 1
fi
