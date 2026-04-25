package com.ashadujjaman.imagecropprocessor;

import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class ImageManager {
    private final Context context;
    private Uri cameraUri;

    public ImageManager(Context context) { this.context = context; }

    public void openGallery(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launcher.launch(intent);
    }

    public void openCamera(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            String time = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
            File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            photoFile = File.createTempFile("IMG_" + time, ".jpg", dir);
        } catch (IOException ignored) {}

        if (photoFile != null) {
            cameraUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
            launcher.launch(intent);
        }
    }

    public void startCrop(Uri uri, CropOptions options, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, CustomCropActivity.class);
        intent.putExtra("imageUri", uri);
        intent.putExtra("options", options);
        launcher.launch(intent);
    }

    public Uri getCameraUri() { return cameraUri; }
}
