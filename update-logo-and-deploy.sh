#!/bin/bash

# 🎨 Update App Logo and Deploy Script

echo "🎨 CallTracker Pro - Logo Update & Deploy"
echo "========================================"

# Copy the new logo
echo "📱 Updating app logo..."
if [ -f "/Users/arsalanahmed507gmail.com/Desktop/CallTrackerPro/ic_logo_light.jpg" ]; then
    # Copy to drawable for use in app
    cp "/Users/arsalanahmed507gmail.com/Desktop/CallTrackerPro/ic_logo_light.jpg" "app/src/main/res/drawable/ic_logo_light.jpg"
    echo "✅ Logo copied to drawable folder"
    
    # For launcher icon, we'll use the existing one for now
    # (Full launcher icon replacement requires specific resolutions)
    echo "📝 Note: Using new logo in app, launcher icon unchanged"
else
    echo "⚠️  Logo file not found at desktop location"
fi

# Update release notes with logo update
cat > release-notes.txt << EOF
🚀 CallTracker Pro - Updated with New Logo!

✨ What's New:
- NEW: Updated app logo design
- ✅ Complete CRM dashboard system
- ✅ Real-time call tracking
- ✅ Multi-tenant organization support
- ✅ Enhanced ticket management
- ✅ User authentication & management
- ✅ Offline demo mode for testing

🔧 Recent Updates:
- Updated app branding with new logo
- Fixed DNS resolution issues
- Added comprehensive error handling
- Improved network connectivity
- Enhanced login experience

📱 Testing Instructions:
- Use demo credentials: anas@anas.com / Anas@1234
- Try demo mode if backend unavailable
- Check new logo in app interface
- Test all CRM features

Generated: $(date)
EOF

echo "📝 Updated release notes with logo information"

# Build new APK
echo "🔨 Building new APK with updated logo..."
./gradlew clean assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful with new logo!"
    
    # Check if we have Firebase app ID configured
    if grep -q "YOUR_FIREBASE_APP_ID" deploy.sh; then
        echo "⚠️  Please update deploy.sh with your Firebase App ID first!"
        echo "📋 Steps:"
        echo "1. Go to Firebase Console"
        echo "2. Copy your App ID (format: 1:123456789:android:abc123def456)"
        echo "3. Edit deploy.sh and replace YOUR_FIREBASE_APP_ID"
        echo "4. Then run: ./deploy.sh"
    else
        echo "🚀 Deploying to Firebase..."
        ./deploy.sh
        
        if [ $? -eq 0 ]; then
            echo "🎉 SUCCESS! New version with logo deployed!"
            echo "📧 Check your email for download link"
            echo "📱 Download and install to see the new logo"
        else
            echo "❌ Deploy failed - check Firebase configuration"
        fi
    fi
    
    # Show APK info
    if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
        APK_SIZE=$(du -h "app/build/outputs/apk/debug/app-debug.apk" | cut -f1)
        echo "📊 New APK Size: $APK_SIZE"
        echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
    fi
else
    echo "❌ Build failed - check error messages above"
fi

echo ""
echo "🎨 Logo Update Summary:"
echo "- ✅ Logo copied to app resources"
echo "- ✅ Release notes updated"
echo "- ✅ New APK built (if successful)"
echo "- 📱 Ready for testing on device"