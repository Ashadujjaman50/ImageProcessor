package com.ashadujjaman.imagecropprocessor;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.Serializable;

public class CropOptions implements Serializable {
    public enum FrameType {
        RECTANGLE, CIRCLE
    }

    public boolean showRotation = true;
    public boolean showFlip = true;
    public float aspectRatioX = 0; 
    public float aspectRatioY = 0;
    public boolean isFixedAspectRatio = false;
    public boolean showGuides = true;
    public FrameType frameType = FrameType.RECTANGLE;
    public boolean defaultScaleEnabled = true;

    public int toolbarColor = Color.parseColor("#46A35C");
    public int toolbarTitleColor = Color.WHITE;
    public String toolbarTitle = "Crop Image";

    // uCrop-like options
    public Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    public int compressQuality = 100;
    public int statusBarColor = Color.parseColor("#D6E4D7");
    public int activeWidgetColor = Color.parseColor("#FFFFFF");
    public int maxResultWidth = 0;
    public int maxResultHeight = 0;
    
    // Control panel option
    public int controlPanelColor = Color.parseColor("#1E1E1E");

    public static class Builder {
        private final CropOptions options = new CropOptions();

        public Builder setRotationEnabled(boolean enabled) {
            options.showRotation = enabled;
            return this;
        }

        public Builder setFlipEnabled(boolean enabled) {
            options.showFlip = enabled;
            return this;
        }

        public Builder setAspectRatio(float x, float y) {
            options.aspectRatioX = x;
            options.aspectRatioY = y;
            options.isFixedAspectRatio = true;
            return this;
        }

        public Builder setToolbarConfig(int bgColor, int titleColor, String title) {
            options.toolbarColor = bgColor;
            options.toolbarTitleColor = titleColor;
            options.toolbarTitle = title;
            return this;
        }

        public Builder setCompressionFormat(Bitmap.CompressFormat format) {
            options.compressFormat = format;
            return this;
        }

        public Builder setCompressionQuality(int quality) {
            options.compressQuality = quality;
            return this;
        }

        public Builder setStatusBarColor(int color) {
            options.statusBarColor = color;
            return this;
        }

        public Builder setActiveWidgetColor(int color) {
            options.activeWidgetColor = color;
            return this;
        }

        public Builder setMaxResultSize(int width, int height) {
            options.maxResultWidth = width;
            options.maxResultHeight = height;
            return this;
        }

        public Builder setShowGuides(boolean show) {
            options.showGuides = show;
            return this;
        }

        public Builder setControlPanelColor(int color) {
            options.controlPanelColor = color;
            return this;
        }

        public Builder setFrameType(FrameType type) {
            options.frameType = type;
            return this;
        }

        public Builder setDefaultScaleEnabled(boolean enabled) {
            options.defaultScaleEnabled = enabled;
            return this;
        }

        public CropOptions build() {
            return options;
        }
    }
}
