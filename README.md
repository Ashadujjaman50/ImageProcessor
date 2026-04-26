# ImageCropProcessor 📸

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
        .setToolbarConfig(Color.parseColor("#1E88E5"), Color.WHITE, "Crop Photo")

        // Status Bar (Darker version of primary)
        .setStatusBarColor(Color.parseColor("#1565C0"))

        // Active controls (Accent color)
        .setActiveWidgetColor(Color.parseColor("#FFB300"))

        // Background panel (Dark theme)
        .setControlPanelColor(Color.parseColor("#121212"))

        // Guides
        .setShowGuides(true)

        // Compression
        .setCompression(Bitmap.CompressFormat.JPEG, 85)

        // Max size
        .setMaxResultSize(1080, 1080)

        .build();
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
