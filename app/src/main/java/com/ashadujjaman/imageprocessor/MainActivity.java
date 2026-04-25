package com.ashadujjaman.imageprocessor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ashadujjaman.imagecropprocessor.CropOptions;
import com.ashadujjaman.imagecropprocessor.ImageManager;

public class MainActivity extends AppCompatActivity {

    private ImageManager imageManager;
    private CropOptions options;
    private ImageView ivCroppedImage;

    // ১. ক্রপ রেজাল্ট হ্যান্ডেলার
    private final ActivityResultLauncher<Intent> cropResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri croppedUri = result.getData().getParcelableExtra("croppedUri");
                    if (croppedUri != null) {
                        ivCroppedImage.setImageURI(croppedUri);
                        Toast.makeText(this, "Image cropped successfully!", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == RESULT_CANCELED && result.getData() != null) {
                    String action = result.getData().getStringExtra("action");
                    if ("closed".equals(action)) {
                        Toast.makeText(this, "Crop cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // ২. ক্যামেরা লঞ্চার
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
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

    // ৩. পারমিশন লঞ্চার
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    imageManager.openCamera(cameraLauncher);
                } else {
                    Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // ৪. গ্যালারি লঞ্চার
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        ivCroppedImage = findViewById(R.id.ivCroppedImage);
        imageManager = new ImageManager(this);

        // লাইব্রেরির সব নতুন অপশন এখান থেকে কন্ট্রোল করা হচ্ছে
        options = new CropOptions.Builder()
                .setAspectRatio(340, 410)                    // আপনার কাঙ্খিত রেশিও
                .setRotationEnabled(true)
                .setFlipEnabled(true)
                .setToolbarConfig(Color.parseColor("#46A35C"), Color.WHITE, "Crop Photo")
                .setStatusBarColor(Color.parseColor("#E6E6E6"))
                .setActiveWidgetColor(Color.parseColor("#02B860"))
                .setControlPanelColor(Color.parseColor("#FFFFFF"))
                .setCompression(Bitmap.CompressFormat.JPEG, 80)
                .setMaxResultSize(1080, 1080)
                .setShowGuides(true)
                .build();

        // গ্যালারি বাটন
        findViewById(R.id.btnOpenGallery).setOnClickListener(v -> imageManager.openGallery(galleryLauncher));

        // ক্যামেরা বাটন (পারমিশন চেক সহ)
        findViewById(R.id.btnOpenCamera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                imageManager.openCamera(cameraLauncher);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }
}