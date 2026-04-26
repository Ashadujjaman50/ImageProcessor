package com.ashadujjaman.imagecropprocessor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class CustomCropActivity extends AppCompatActivity {
    private TouchCropView touchCropView;
    private RotationScaleView rotationScaleView;
    private TextView tvRotationAngle;
    private CropOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_custom_crop);

        options = IntentCompat.getSerializableExtra(getIntent(), "options", CropOptions.class);
        Uri uri = IntentCompat.getParcelableExtra(getIntent(), "imageUri", Uri.class);

        setupWindowInsets();
        setupToolbar();
        setupUI();
        setupBackPressHandler();

        if (uri != null) {
            loadBitmap(uri);
        }
    }

    private void setupWindowInsets() {
        View statusBarSpacer = findViewById(R.id.statusBarSpacer);
        View navigationBarSpacer = findViewById(R.id.navigationBarSpacer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            if (statusBarSpacer != null) {
                ViewGroup.LayoutParams params = statusBarSpacer.getLayoutParams();
                params.height = systemBars.top;
                statusBarSpacer.setLayoutParams(params);
                if (options != null) statusBarSpacer.setBackgroundColor(options.statusBarColor);
            }

            if (navigationBarSpacer != null) {
                ViewGroup.LayoutParams params = navigationBarSpacer.getLayoutParams();
                params.height = systemBars.bottom;
                navigationBarSpacer.setLayoutParams(params);
                if (options != null) navigationBarSpacer.setBackgroundColor(options.controlPanelColor);
            }

            return insets;
        });
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
                        // এটি খুবই গুরুত্বপূর্ণ: ফ্রেম টাইপ সেট করা
                        touchCropView.setFrameType(options.frameType);
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

            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);

            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
            if (controller != null) {
                controller.setAppearanceLightStatusBars(isColorLight(options.statusBarColor));
            }
        }
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            sendCancelResult();
            finish();
        });
    }

    private boolean isColorLight(int color) {
        if (color == Color.TRANSPARENT) return false;
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }

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
        rotationScaleView = findViewById(R.id.rotationScaleView);
        tvRotationAngle = findViewById(R.id.tvRotationAngle);

        View btnRotate = findViewById(R.id.btnRotate);
        View btnFlip = findViewById(R.id.btnFlip);
        View btnDone = findViewById(R.id.btnDone);
        LinearLayout controlPanel = findViewById(R.id.controlPanel);

        // ১. হাত দিয়ে টাচ করে ঘোরানোর লিসেনার (নতুন যোগ করা হলো)
        if (touchCropView != null) {
            touchCropView.setOnTouchRotationListener(currentRot -> {
                tvRotationAngle.setText(String.format(Locale.getDefault(), "%.1f°", currentRot));
                if (rotationScaleView != null) {
                    rotationScaleView.setRotation(currentRot);
                }
            });
        }

        // ২. নিচের স্কেল দিয়ে ঘোরানোর লিসেনার
        if (rotationScaleView != null) {
            rotationScaleView.setOnRotationChangeListener(deltaAngle -> {
                touchCropView.rotate(deltaAngle);
                float currentRot = touchCropView.getCurrentRotation();
                tvRotationAngle.setText(String.format(Locale.getDefault(), "%.1f°", currentRot));
                rotationScaleView.setRotation(currentRot);
            });
        }

        if (options != null) {
            btnRotate.setVisibility(options.showRotation ? View.VISIBLE : View.GONE);
            btnFlip.setVisibility(options.showFlip ? View.VISIBLE : View.GONE);

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

        // ৩. রোটেশন বাটনে ক্লিক লিসেনার
        btnRotate.setOnClickListener(v -> {
            touchCropView.rotate(90);
            float currentRot = touchCropView.getCurrentRotation();
            tvRotationAngle.setText(String.format(Locale.getDefault(), "%.1f°", currentRot));
            if (rotationScaleView != null) {
                rotationScaleView.setRotation(currentRot);
            }
        });

        btnFlip.setOnClickListener(v -> touchCropView.flip());
        btnDone.setOnClickListener(v -> performFinalCrop());
    }

    private void performFinalCrop() {
        Bitmap croppedBitmap = touchCropView.getCroppedBitmap();
        if (croppedBitmap == null) return;

        if (options != null && options.maxResultWidth > 0 && options.maxResultHeight > 0) {
            int originalWidth = croppedBitmap.getWidth();
            int originalHeight = croppedBitmap.getHeight();
            float aspectRatio = (float) originalWidth / originalHeight;

            int finalWidth, finalHeight;
            if (aspectRatio > (float) options.maxResultWidth / options.maxResultHeight) {
                finalWidth = options.maxResultWidth;
                finalHeight = (int) (finalWidth / aspectRatio);
            } else {
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
