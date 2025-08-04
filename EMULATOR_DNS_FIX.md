# 🚀 Android Emulator DNS Fix - WORKING SOLUTION

## ❌ Problem
Android emulator cannot resolve `calltrackerpro-backend.vercel.app`

## ✅ IMMEDIATE SOLUTION

### Option 1: Cold Boot Emulator (Recommended)
1. **Close Android Studio completely**
2. **Close emulator completely**
3. **Open Android Studio**
4. **Tools** → **AVD Manager**
5. **Click dropdown arrow** next to emulator → **Cold Boot Now**
6. **Wait for full restart** (2-3 minutes)
7. **Try login again**

### Option 2: Reset Emulator DNS
1. In emulator, go to **Settings**
2. **Network & Internet** → **Advanced** → **Private DNS**
3. Select **Private DNS provider hostname**
4. Enter: `dns.google`
5. **Save** and restart app

### Option 3: Use Different Emulator
1. **AVD Manager** → **Create Virtual Device**
2. Choose **Pixel 6** or **Pixel 7**
3. **API 33** or **API 34**
4. **Download** if needed
5. **Launch new emulator**

### Option 4: Use Physical Device
1. **Enable Developer Options**:
   - Settings → About Phone → Build Number (tap 7 times)
2. **Enable USB Debugging**:
   - Settings → Developer Options → USB Debugging
3. **Connect via USB**
4. **Trust computer** when prompted

## 🔧 Alternative: Manual DNS Override

If still failing, add this to your WiFi (on device/emulator):
- **DNS 1**: `8.8.8.8`
- **DNS 2**: `8.8.4.4`

## 🎯 Test Credentials
```
Email: anas@anas.com
Password: Anas@1234
```

## 📱 Why This Happens
- Android emulator uses host machine's DNS
- Some corporate networks block Vercel domains
- Emulator DNS cache issues
- Windows DNS resolver conflicts

**Cold Boot** usually fixes 90% of cases! 🚀