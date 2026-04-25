package com.ashadujjaman.imagecropprocessor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CustomCropActivity extends AppCompatActivity {
    private TouchCropView touchCropView;
    private CropOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crop);

        options = IntentCompat.getSerializableExtra(getIntent(), "options", CropOptions.class);
        Uri uri = IntentCompat.getParcelableExtra(getIntent(), "imageUri", Uri.class);

        setupToolbar();
        setupUI();
        setupBackPressHandler();

        if (uri != null) {
            loadBitmap(uri);
        }
    }

    private void loadBitmap(Uri uri) {
        new Thread(() -> {
            try {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                Bitmap decodedBitmap = ImageDecoder.decodeBitmap(source, (decoder, info, s) -> {
                    decoder.setMutableRequired(true);
                });
                runOnUiThread(() -> {
                    touchCropView.setBitmap(decodedBitmap);
                    if (options != null) {
                        float ratio = (options.aspectRatioX > 0 && options.aspectRatioY > 0) 
                            ? options.aspectRatioX / options.aspectRatioY 
                            : 1.0f;
                        touchCropView.setAspectRatio(ratio, options.isFixedAspectRatio);
                        touchCropView.setShowGuides(options.showGuides);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.cropToolbar);
        if (options != null) {
            toolbar.setBackgroundColor(options.toolbarColor);
            toolbar.setTitleTextColor(options.toolbarTitleColor);
            toolbar.setTitle(options.toolbarTitle);

            // Set Status Bar Color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(options.statusBarColor);
        }
        setSupportActionBar(toolbar);
        
        // টুলবারের ক্লোজ বাটন লিসেনার
        toolbar.setNavigationOnClickListener(v -> {
            sendCancelResult();
            finish();
        });
    }

    // মডার্ন ব্যাক বাটন হ্যান্ডলার (Non-deprecated way)
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                sendCancelResult();
                finish();
            }
        });
    }

    private void sendCancelResult() {
        Intent intent = new Intent();
        intent.putExtra("action", "closed");
        setResult(RESULT_CANCELED, intent);
    }

    private void setupUI() {
        touchCropView = findViewById(R.id.touchCropView);
        View btnRotate = findViewById(R.id.btnRotate);
        View btnFlip = findViewById(R.id.btnFlip);
        View btnDone = findViewById(R.id.btnDone);
        LinearLayout controlPanel = findViewById(R.id.controlPanel);

        if (options != null) {
            btnRotate.setVisibility(options.showRotation ? View.VISIBLE : View.GONE);
            btnFlip.setVisibility(options.showFlip ? View.VISIBLE : View.GONE);

            
            // Dynamic Control Panel Color
            if (controlPanel != null) {
                controlPanel.setBackgroundColor(options.controlPanelColor);
            }
            
            ImageView ivRotate = findViewById(R.id.ivRotateIcon);
            TextView tvRotate = findViewById(R.id.tvRotateText);
            ImageView ivFlip = findViewById(R.id.ivFlipIcon);
            TextView tvFlip = findViewById(R.id.tvFlipText);
            ImageView ivDoneIcon = findViewById(R.id.ivDoneIcon);
            TextView tvDoneText = findViewById(R.id.tvDoneText);

            if (ivRotate != null) ivRotate.setColorFilter(options.activeWidgetColor);
            if (tvRotate != null) tvRotate.setTextColor(options.activeWidgetColor);
            if (ivFlip != null) ivFlip.setColorFilter(options.activeWidgetColor);
            if (tvFlip != null) tvFlip.setTextColor(options.activeWidgetColor);
            if (ivDoneIcon != null) ivDoneIcon.setColorFilter(options.toolbarColor);
            if (tvDoneText != null) tvDoneText.setTextColor(options.toolbarColor);
        }

        btnRotate.setOnClickListener(v -> touchCropView.rotate(90));
        btnFlip.setOnClickListener(v -> touchCropView.flip());
        btnDone.setOnClickListener(v -> performFinalCrop());
    }

    private void performFinalCrop() {
        Bitmap croppedBitmap = touchCropView.getCroppedBitmap();
        if (croppedBitmap == null) return;

        // আপনার রিকোয়ারমেন্ট অনুযায়ী Max Result Size ক্যালকুলেশন
        if (options != null && options.maxResultWidth > 0 && options.maxResultHeight > 0) {
            int originalWidth = croppedBitmap.getWidth();
            int originalHeight = croppedBitmap.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;

            int finalWidth, finalHeight;

            // Aspect Ratio ঠিক রেখে Max Size এর ভেতরে স্কেলিং
            if (aspectRatio > (float) options.maxResultWidth / options.maxResultHeight) {
                // Width বড় হলে Width কে Max ধরে Height ক্যালকুলেট করা
                finalWidth = options.maxResultWidth;
                finalHeight = (int) (finalWidth / aspectRatio);
            } else {
                // Height বড় হলে Height কে Max ধরে Width ক্যালকুলেট করা
                finalHeight = options.maxResultHeight;
                finalWidth = (int) (finalHeight * aspectRatio);
            }

            croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, finalWidth, finalHeight, true);
        }

        Bitmap finalBitmap = croppedBitmap;
        new Thread(() -> {
            try {
                String fileName = "cropped_" + System.currentTimeMillis() + ".jpg";
                File file = new File(getCacheDir(), fileName);
                FileOutputStream out = new FileOutputStream(file);
                
                // Using dynamic compression format and quality
                Bitmap.CompressFormat format = (options != null) ? options.compressFormat : Bitmap.CompressFormat.JPEG;
                int quality = (options != null) ? options.compressQuality : 100;
                
                finalBitmap.compress(format, quality, out);
                out.flush();
                out.close();

                Uri resultUri = Uri.fromFile(file);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("croppedUri", resultUri);

                runOnUiThread(() -> {
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
