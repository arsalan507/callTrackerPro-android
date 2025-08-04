#!/bin/bash

# 🚀 CallTracker Pro - Complete Setup & Build Script

echo "🔧 CallTracker Pro - Setup & Build"
echo "=================================="

# Check if we're in the right directory
if [ ! -f "app/build.gradle.kts" ]; then
    echo "❌ Error: Please run this script from the Android project root directory"
    exit 1
fi

# Fix Gradle wrapper if missing
echo "🔍 Checking Gradle wrapper..."
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    echo "⚠️  Gradle wrapper JAR missing, downloading..."
    
    # Create wrapper directory if it doesn't exist
    mkdir -p gradle/wrapper
    
    # Download the wrapper JAR
    curl -L "https://github.com/gradle/gradle/raw/v8.11.1/gradle/wrapper/gradle-wrapper.jar" \
         -o "gradle/wrapper/gradle-wrapper.jar"
    
    if [ $? -eq 0 ]; then
        echo "✅ Gradle wrapper JAR downloaded successfully"
    else
        echo "❌ Failed to download Gradle wrapper JAR"
        echo "💡 Manual fix: Download gradle-wrapper.jar and place in gradle/wrapper/"
        exit 1
    fi
fi

# Make gradlew executable
chmod +x gradlew

echo "🧹 Cleaning previous builds..."
./gradlew clean

echo "🔨 Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "🎉 BUILD SUCCESSFUL!"
    echo "📱 APK Location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    
    # Check if APK exists
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
        echo "📊 APK Size: $APK_SIZE"
        echo ""
        echo "🚀 Ready for Firebase deployment!"
        echo "Run: ./deploy.sh (after completing Firebase setup)"
        echo ""
        echo "📋 Next Steps:"
        echo "1. Complete Firebase setup (see FIREBASE_SETUP_GUIDE.md)"
        echo "2. Run ./deploy.sh to deploy to Firebase"
        echo "3. Download APK on your phone from Firebase email"
        echo "4. Test the app!"
    else
        echo "⚠️  APK file not found despite successful build"
    fi
else
    echo ""
    echo "❌ BUILD FAILED"
    echo "📋 Common solutions:"
    echo "1. Check Android Studio is closed"
    echo "2. Run: ./gradlew clean"
    echo "3. Check Java/Android SDK installation"
    echo "4. Try building in Android Studio first"
fi