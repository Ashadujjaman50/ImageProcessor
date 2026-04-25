# ImageCropProcessor 📸

[English](#english) | [বাংলা](#বাংলা)

---

## English

**ImageCropProcessor** is a powerful and customizable Android library that provides image cropping functionality without any third-party dependencies. It allows users to zoom, pan, rotate, and flip images, and crop them according to various aspect ratios.

### ✨ Features
- ✅ **No Third-party Library:** Built entirely using native Android APIs.
- ✅ **Gesture Support:** Supports Pinch to Zoom and Drag to Pan.
- ✅ **Smart Bounce back:** Automatically snaps back and fits the frame if the image is moved outside or zoomed out too much.
- ✅ **Dynamic Customization:** Dynamic control for Toolbar, Status Bar, and Control Panel colors.
- ✅ **Rule of Thirds:** Displays guide lines (grid view) during cropping.
- ✅ **Aspect Ratio:** Support for fixed ratios (e.g., 4:3, 16:9) or free-style cropping.
- ✅ **Compression & Resize:** Granular control over output image quality and maximum dimensions.

### 🚀 Installation

#### Step 1: Add JitPack Repository
Add it to your `settings.gradle.kts` file:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### Step 2: Add Dependency
Add the library to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.Ashadujjaman50:ImageProcessor:1.0.0")
}
```

### 🛠 Usage

1. **Initialize ImageManager:**
```java
ImageManager imageManager = new ImageManager(this);
```

2. **Setup Crop Options:**
```java
CropOptions options = new CropOptions.Builder()
        .setAspectRatio(4, 3)

        // Toolbar (Primary Color)
        .setToolbarConfig(Color.parseColor("#46A35C"), Color.WHITE, "Crop Photo")

        // Status Bar (Darker version of primary)
        .setStatusBarColor(Color.parseColor("#E6E6E6"))

        // Active controls (Accent color)
        .setActiveWidgetColor(Color.parseColor("#02B860"))

        // Background panel (Dark theme)
        .setControlPanelColor(Color.parseColor("#FFFFFF"))

        // Guides
        .setShowGuides(true)

        // Compression
        .setCompression(Bitmap.CompressFormat.JPEG, 85)

        // Max size
        .setMaxResultSize(1080, 1080)

        .build();         
                
```

---

## বাংলা

**ImageCropProcessor** একটি শক্তিশালী এবং কাস্টমাইজযোগ্য অ্যান্ড্রয়েড লাইব্রেরি, যা কোনো থার্ড-পার্টি ডিপেন্ডেন্সি ছাড়াই ইমেজ ক্রপিংয়ের সুবিধা দেয়। এটি ইউজারকে জুম, প্যান, রোটেশন এবং ফ্লিপ করার পাশাপাশি বিভিন্ন অ্যাসপেক্ট রেশিওতে ছবি কাটার সুবিধা প্রদান করে।

### ✨ ফিচারসমূহ
- ✅ **No Third-party Library:** পুরোপুরি নেটিভ অ্যান্ড্রয়েড এপিআই ব্যবহার করে তৈরি।
- ✅ **Gesture Support:** Pinch to Zoom এবং Drag to Pan সুবিধা।
- ✅ **Smart Bounce back:** ইমেজ ফ্রেমের বাইরে চলে গেলে বা ছোট হলে অটোমেটিক ফ্রেমের সাথে ফিট হয়ে যায়।
- ✅ **Dynamic Customization:** টুলবার, স্ট্যাটাস বার, এবং কন্ট্রোল প্যানেলের কালার ডাইনামিকালি সেট করা যায়।
- ✅ **Rule of Thirds:** ক্রপিংয়ের সময় গাইড লাইন বা গ্রিড ভিউ।
- ✅ **Aspect Ratio:** নির্দিষ্ট রেশিও (যেমন: 4:3, 16:9) অথবা ফ্রি ক্রপিং।
- ✅ **Compression & Resize:** আউটপুট ইমেজের কোয়ালিটি এবং ম্যাক্সিমাম সাইজ কন্ট্রোল করার সুবিধা।

### 🚀 ইন্সটলেশন

#### Step 1: JitPack রিপোজিটরি যোগ করুন
আপনার প্রজেক্টের `settings.gradle.kts` ফাইলে এটি যোগ করুন:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

#### Step 2: ডিপেন্ডেন্সি যোগ করুন
আপনার অ্যাপ মডিউলের `build.gradle.kts` ফাইলে লাইব্রেরিটি যোগ করুন:

```kotlin
dependencies {
    implementation("com.github.Ashadujjaman50:ImageProcessor:1.0.0")
}
```

---

## 📋 Permissions

### AndroidManifest.xml
Ensure these permissions are added to your manifest:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

---

## 🤝 Contributing
If you find any bugs or want to add new features, please create an **Issue** or send a **Pull Request**.

## 📄 License
This project is licensed under the MIT License.

---
**Developed by [Ashadujjaman](https://github.com/Ashadujjaman50)**
