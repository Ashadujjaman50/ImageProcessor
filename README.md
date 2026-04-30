# ImageCropProcessor 📸

**ImageCropProcessor** is a lightweight, powerful, and highly customizable Android library for image cropping. Built entirely using native Android APIs, it requires **zero third-party dependencies**, ensuring a small footprint and easy integration.

It provides a smooth user experience with support for zooming, panning, rotating, and flipping, along with specialized frame types like **Circle** and **Rectangle**.

---

### ✨ Features
- ✅ **No Third-party Library:** Built entirely using native Android APIs.
- ✅ **Gesture Support:** Supports Pinch to Zoom, Drag to Pan, and Two-finger Rotation.
- ✅ **Frame Types:** Supports both **RECTANGLE** and **CIRCLE** crop frames.
- ✅ **Smart Bounce back:** Automatically snaps back and fits the frame if the image is moved outside or zoomed out too much.
- ✅ **Improved Stability:** Prioritizes moving/panning over zooming when image edges reach the crop frame.
- ✅ **Dynamic Customization:** Full control over Toolbar, Status Bar, and Control Panel colors.
- ✅ **Advanced Image Controls:** Dedicated buttons for Rotate (90°), Flip (Horizontal), and Scale (Manual Rotation Mode).
- ✅ **Rule of Thirds:** Displays optional guide lines (grid view) for better composition.
- ✅ **Compression & Resize:** Granular control over output image quality, format, and dimensions.

---

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
    implementation("com.github.Ashadujjaman50:ImageProcessor:1.0.2")
}
```

---

### 🛠 Usage

#### 1. Initialize Result Launchers
Register for activity results in your `Activity` or `Fragment` to handle the cropped image and source selection.

```java
private ImageManager imageManager;
private CropOptions options;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    imageManager = new ImageManager(this);

    // 1. Crop Result Handler
    ActivityResultLauncher<Intent> cropResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = result.getData().getParcelableExtra("croppedUri");
                    if (croppedUri != null) {
                        ivCroppedImage.setImageURI(croppedUri);
                    }
                }
            }
    );

    // 2. Camera Launcher
    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Uri cameraUri = imageManager.getCameraUri();
                    if (cameraUri != null) {
                        imageManager.startCrop(cameraUri, options, cropResultLauncher);
                    }
                }
            }
    );

    // 3. Gallery Launcher 
    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        imageManager.startCrop(selectedUri, options, cropResultLauncher);
                    }
                }
            }
    );

    // 4. Permission Launcher for Camera
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    imageManager.openCamera(cameraLauncher);
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            }
    );
}
```

#### 2. Handle Camera Permission & Open Camera, Gallery
In your click listener, check for permission before opening the camera.

```java
//Click Gallery Button
btnGallery.setOnClickListener(v -> imageManager.openGallery(galleryLauncher));

//Click Camera Button
btnOpenCamera.setOnClickListener(v -> {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
        imageManager.openCamera(cameraLauncher);
    } else {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }
});
```

#### 3. Configure `CropOptions`
```java
options = new CropOptions.Builder()
        .setFrameType(CropOptions.Type.RECTANGLE)   // Choose RECTANGLE or CIRCLE
        .setAspectRatio(3, 2)                      // Mandatory (1,1) for CIRCLE frame
        .setDefaultScaleEnabled(true)              // Set default Scale state (true/false)
        .setToolbarConfig(Color.parseColor("#46A35C"), Color.WHITE, "Crop Photo")
        .setStatusBarColor(Color.parseColor("#D6E4D7"))
        .setActiveWidgetColor(Color.parseColor("#02B860"))
        .setControlPanelColor(Color.parseColor("#1B1B1B"))
        .setCompressionFormat(Bitmap.CompressFormat.JPEG)
        .setCompressionQuality(80)                 // Set image quality (0-100)
        .setMaxResultSize(1080, 1080)
        .setShowGuides(true)
        .build();
```

> **💡 Note:** When using `FrameType.CIRCLE`, it is mandatory to set the Aspect Ratio to `(1, 1)` for a perfect circular result.

---

### 🎮 Editor Controls

The editor provides intuitive controls to fine-tune your crop:

| Control | Description |
| :--- | :--- |
| **Rotate** | Rotates the image **90 degrees clockwise** with each click. |
| **Flip** | Flips the image **horizontally** (Mirror effect). Useful for fixing selfies. |
| **Scale (On/Off)** | **Toggles Rotation Mode**. When **ON**, it enables manual rotation via touch gestures and shows a precise rotation scale. When **OFF**, rotation UI is hidden and rotation is locked to prevent accidental tilting. |
| **Done (✔)** | Processes the crop and returns the result. |

---

## 📋 Permissions
Add this to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
```
*Note: Storage permissions are typically handled via system pickers and don't require manual declaration for modern Android versions.*

---

## 🤝 Contributing
Contributions are welcome! Please open an **Issue** or submit a **Pull Request** for any improvements.

## 📄 License
This project is licensed under the MIT License.

---
**Developed with ❤️ by [Ashadujjaman](https://github.com/Ashadujjaman50)**
