#!/bin/bash

# 🚀 CallTracker Pro - Firebase Deployment Script

echo "🔨 Building CallTracker Pro APK..."

# Clean and build debug APK
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    # Check if APK exists
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo "📱 APK found at: $APK_PATH"
        
        # Update release notes with current date
        sed -i '' "s/Generated: .*/Generated: $(date)/" release-notes.txt
        
        echo "🚀 Deploying to Firebase App Distribution..."
        
        # Deploy to Firebase (you'll need to add your app ID)
        firebase appdistribution:distribute "$APK_PATH" \
            --app "YOUR_FIREBASE_APP_ID" \
            --groups "testers" \
            --release-notes-file "./release-notes.txt"
        
        if [ $? -eq 0 ]; then
            echo "🎉 Deployment successful!"
            echo "📧 Check your email for download link"
        else
            echo "❌ Deployment failed - check Firebase configuration"
        fi
    else
        echo "❌ APK not found - build may have failed"
    fi
else
    echo "❌ Build failed - check gradle errors"
fi