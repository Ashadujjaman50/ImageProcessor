package com.ashadujjaman.imagecropprocessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.Nullable;

public class TouchCropView extends View {

    private Bitmap bitmap;
    private final Matrix matrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private final RectF cropRect = new RectF();
    private float aspectRatio = 1f;
    private boolean isFixedAspectRatio = false;

    private final Paint overlayPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint guidePaint = new Paint();

    private boolean showGuides = true;

    public TouchCropView(Context context) {
        super(context);
        init(context);
    }

    public TouchCropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        overlayPaint.setColor(Color.parseColor("#88000000"));
        overlayPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(5f);
        borderPaint.setStyle(Paint.Style.STROKE);

        guidePaint.setColor(Color.parseColor("#AAFFFFFF"));
        guidePaint.setStrokeWidth(2f);
        guidePaint.setStyle(Paint.Style.STROKE);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (bitmap != null && getWidth() > 0) {
            resetImage();
        }
        invalidate();
    }

    public void setAspectRatio(float ratio, boolean fixed) {
        this.aspectRatio = ratio;
        this.isFixedAspectRatio = fixed;
        if (getWidth() > 0) {
            calculateCropRect();
            resetImage();
        }
        invalidate();
    }

    public void setShowGuides(boolean show) {
        this.showGuides = show;
        invalidate();
    }

    private void resetImage() {
        if (bitmap == null || getWidth() == 0 || cropRect.isEmpty()) return;

        matrix.reset();
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        // Ensure the image covers the entire crop frame (both width and height)
        float scale = Math.max(cropRect.width() / bitmapWidth, cropRect.height() / bitmapHeight);

        matrix.postScale(scale, scale);
        // Center the image within the crop frame
        matrix.postTranslate(cropRect.centerX() - (bitmapWidth * scale) / 2f, 
                             cropRect.centerY() - (bitmapHeight * scale) / 2f);
        checkBounds();
    }

    private void calculateCropRect() {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0) return;

        float margin = 20 * getResources().getDisplayMetrics().density;
        float maxWidth = viewWidth - 2 * margin;
        float maxHeight = viewHeight - 2 * margin;

        float width, height;
        
        width = maxWidth;
        height = width / aspectRatio;
        
        // যদি হাইট স্ক্রিনের বাইরে চলে যায়, তবে হাইট অনুযায়ী অ্যাডজাস্ট করা
        if (height > maxHeight) {
            height = maxHeight;
            width = height * aspectRatio;
        }

        float left = (viewWidth - width) / 2;
        float top = (viewHeight - height) / 2;
        cropRect.set(left, top, left + width, top + height);
    }

    private void checkBounds() {
        if (bitmap == null || cropRect.isEmpty()) return;

        RectF bitmapRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrix.mapRect(bitmapRect);

        // 1. Scale Check: Zoom back in if the image becomes smaller than the frame
        if (bitmapRect.width() < cropRect.width() - 1 || bitmapRect.height() < cropRect.height() - 1) {
            float scale = Math.max(cropRect.width() / bitmapRect.width(), cropRect.height() / bitmapRect.height());
            matrix.postScale(scale, scale, cropRect.centerX(), cropRect.centerY());
            
            bitmapRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            matrix.mapRect(bitmapRect);
        }

        float dx = 0, dy = 0;
        if (bitmapRect.left > cropRect.left) dx = cropRect.left - bitmapRect.left;
        if (bitmapRect.right < cropRect.right) dx = cropRect.right - bitmapRect.right;
        if (bitmapRect.top > cropRect.top) dy = cropRect.top - bitmapRect.top;
        if (bitmapRect.bottom < cropRect.bottom) dy = cropRect.bottom - bitmapRect.bottom;

        matrix.postTranslate(dx, dy);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            calculateCropRect();
            resetImage();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) return;

        canvas.drawBitmap(bitmap, matrix, null);

        canvas.drawRect(0, 0, getWidth(), cropRect.top, overlayPaint);
        canvas.drawRect(0, cropRect.bottom, getWidth(), getHeight(), overlayPaint);
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, overlayPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, overlayPaint);

        canvas.drawRect(cropRect, borderPaint);

        if (showGuides) {
            float thirdWidth = cropRect.width() / 3;
            float thirdHeight = cropRect.height() / 3;

            canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, cropRect.left + thirdWidth, cropRect.bottom, guidePaint);
            canvas.drawLine(cropRect.left + 2 * thirdWidth, cropRect.top, cropRect.left + 2 * thirdWidth, cropRect.bottom, guidePaint);
            canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, cropRect.right, cropRect.top + thirdHeight, guidePaint);
            canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdHeight, cropRect.right, cropRect.top + 2 * thirdHeight, guidePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            checkBounds();
            invalidate();
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            invalidate();
            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            matrix.postTranslate(-distanceX, -distanceY);
            invalidate();
            return true;
        }
    }

    public Bitmap getCroppedBitmap() {
        if (bitmap == null) return null;

        matrix.invert(inverseMatrix);
        RectF mappedCropRect = new RectF();
        inverseMatrix.mapRect(mappedCropRect, cropRect);

        int left = Math.max(0, (int) mappedCropRect.left);
        int top = Math.max(0, (int) mappedCropRect.top);
        int width = Math.min(bitmap.getWidth() - left, (int) mappedCropRect.width());
        int height = Math.min(bitmap.getHeight() - top, (int) mappedCropRect.height());

        if (width <= 0 || height <= 0) return null;

        return Bitmap.createBitmap(bitmap, left, top, width, height);
    }

    public void rotate(float degrees) {
        if (bitmap == null) return;
        matrix.postRotate(degrees, cropRect.centerX(), cropRect.centerY());
        checkBounds();
        invalidate();
    }

    public void flip() {
        if (bitmap == null) return;
        // Using scale -1 for flip. We need to be careful with the center point.
        matrix.postScale(-1, 1, cropRect.centerX(), cropRect.centerY());
        checkBounds();
        invalidate();
    }
}
