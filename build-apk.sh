#!/bin/bash

# 🚀 Simple APK Build Script

echo "🔨 Building CallTracker Pro APK..."

# Method 1: Try gradlew (if wrapper works)
if [ -f "gradlew" ] && [ -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "📱 Using Gradle wrapper..."
    ./gradlew clean assembleDebug
elif command -v gradle &> /dev/null; then
    # Method 2: Use system gradle
    echo "📱 Using system Gradle..."
    gradle clean assembleDebug
else
    echo "❌ Neither gradlew nor gradle found"
    echo "💡 Try running setup-and-build.sh first"
    exit 1
fi

# Check if build succeeded
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ APK built successfully!"
    echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Show APK info
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo "❌ APK build failed"
    echo "💡 Check the error messages above"
fi