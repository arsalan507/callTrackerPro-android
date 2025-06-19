# 📱 TeleCRM Android App

**Smart Call Logging & CRM System with AI-Powered Insights**

A modern Android application that automatically captures SIM call data and syncs with TeleCRM backend for intelligent business insights.

## ✨ Features

- 📞 **Automatic Call Detection** - Real-time monitoring of incoming/outgoing calls
- 🔄 **Real-time Sync** - Seamless sync with TeleCRM backend
- 📊 **Professional Dashboard** - Call statistics and analytics
- 🧠 **AI-Ready** - Prepared for transcription and sentiment analysis
- 🔐 **Permission Management** - Secure access to call logs and contacts

## 🏗 Tech Stack

- **Platform**: Android 5.0+ (API 21)
- **Language**: Java
- **UI**: Material Design Components
- **Networking**: Retrofit + OkHttp
- **Backend**: Node.js TeleCRM API

## 🚀 Quick Start

### Prerequisites
- Android Studio 4.0+
- TeleCRM Backend Server running
- Android device or emulator

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/arsalan507/telecrm-android.git
cd telecrm-android

Open in Android Studio


Launch Android Studio
File → Open → Select project directory
Wait for Gradle sync


Configure Backend Connection

java// Update API endpoint in ApiService.java
String BASE_URL = "http://YOUR_SERVER_IP:5000/";

Build and Run


Connect Android device or start emulator
Click Run ▶ or press Shift+F10
Grant permissions when prompted

🔧 Required Permissions
xml<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
📡 API Integration
Main Endpoints

POST /api/call-logs - Create new call log
GET /api/call-logs - Fetch all call logs
GET /api/call-logs/test - Test connection

Sample Usage
java// Test API connection
NetworkHelper.testApiConnection(new ApiCallback<String>() {
    @Override
    public void onSuccess(String data) {
        // Connection successful
    }
    
    @Override
    public void onError(String error) {
        // Handle error
    }
});
🎯 Current Status
✅ Completed

 Professional UI with dashboard
 Permission system
 Network layer architecture
 API service interfaces
 Backend connectivity

🚧 In Progress

 Real call detection
 Background monitoring service
 AI integration features

📱 Screenshots
Show Image
🤝 Contributing

Fork the repository
Create feature branch
Commit changes
Push to branch
Open Pull Request

📄 License
MIT License - see LICENSE file for details.
🔗 Related Projects

TeleCRM Backend - Node.js backend with MongoDB

📞 Support

Issues: GitHub Issues
Email: arsalan507@gmail.com


Built with ❤️ for modern businesses