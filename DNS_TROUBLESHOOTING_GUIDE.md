# 🔧 DNS Resolution Fix Guide for CallTracker Pro Android

## ❌ Issue
`java.net.UnknownHostException: Unable to resolve host "calltrackerpro-backend.vercel.app"`

## ✅ Quick Solutions

### 1. **Android Emulator Solutions**

#### Option A: Restart Emulator with DNS Settings
```bash
# Close current emulator completely
# Start emulator with custom DNS
emulator -avd YOUR_AVD_NAME -dns-server 8.8.8.8,8.8.4.4
```

#### Option B: Configure Emulator Network
1. In Android Studio: `Tools` → `AVD Manager`
2. Click the pencil icon next to your emulator
3. Click `Advanced Settings`
4. Set Network Speed: `Full`
5. Set Network Latency: `None`

### 2. **Physical Device Solutions**

#### Change WiFi DNS Settings:
1. Go to `Settings` → `WiFi`
2. Long press your connected network
3. Select `Modify Network`
4. Advanced options → `IP Settings` → `Static`
5. Set DNS 1: `8.8.8.8`
6. Set DNS 2: `8.8.4.4`

### 3. **Development Environment**

#### Check Your Internet Connection:
```bash
# Test DNS resolution from your computer
nslookup calltrackerpro-backend.vercel.app
ping calltrackerpro-backend.vercel.app
```

#### Verify Backend is Running:
Open browser: https://calltrackerpro-backend.vercel.app

### 4. **Code Improvements Applied**

✅ **Enhanced Network Configuration:**
- Increased timeouts (45s connect, 60s total)
- Added retry on connection failure
- Enhanced error handling with diagnostics

✅ **Network Security Config:**
- Proper certificate handling
- Vercel domain support
- System DNS trust anchors

✅ **Diagnostics & Logging:**
- NetworkHelper utility class
- Real-time network status checking
- DNS resolution testing

## 🔄 **Retry Steps**

1. **Cold restart** your emulator
2. **Clear app data**: Settings → Apps → CallTracker Pro → Storage → Clear Data
3. **Try again** with enhanced error handling

## 🚀 **Test Credentials**

```java
Email: androidtest@example.com
Password: Android@123
```

Alternative:
```java
Email: anas@anas.com  
Password: Anas@1234
```

## 📱 **If Still Failing**

The app now includes comprehensive logging. Check Android Studio Logcat for:
- Network diagnostics
- DNS resolution tests
- Connectivity status

**Filter logs by:** `calltrackerpro` or `LoginActivity`