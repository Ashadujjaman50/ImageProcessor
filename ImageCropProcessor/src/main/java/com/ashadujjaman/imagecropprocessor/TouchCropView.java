package com.ashadujjaman.imagecropprocessor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
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

    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private final RectF cropRect = new RectF();
    private float aspectRatio = 1f;
    private boolean isFixedAspectRatio = false;

    private final Paint overlayPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint guidePaint = new Paint();
    private final Paint cornerPaint = new Paint();

    private boolean showGuides = true;
    private float lastAngle = 0f;
    private float currentRotation = 0f;
    private CropOptions.FrameType frameType = CropOptions.FrameType.RECTANGLE;
    private OnTouchRotationListener rotationListener;
    private boolean rotationEnabled = true;

    public interface OnTouchRotationListener {
        void onRotationChanged(float totalRotation);
    }

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
        borderPaint.setStrokeWidth(3f);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);

        guidePaint.setColor(Color.parseColor("#AAFFFFFF"));
        guidePaint.setStrokeWidth(1.5f);
        guidePaint.setStyle(Paint.Style.STROKE);

        cornerPaint.setColor(Color.WHITE);
        cornerPaint.setStrokeWidth(8f);
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeCap(Paint.Cap.ROUND);
        cornerPaint.setAntiAlias(true);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public void setRotationEnabled(boolean enabled) {
        this.rotationEnabled = enabled;
    }

    public boolean isRotationEnabled() {
        return rotationEnabled;
    }

    public void setOnTouchRotationListener(OnTouchRotationListener listener) {
        this.rotationListener = listener;
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

    public void setFrameType(CropOptions.FrameType type) {
        this.frameType = type;
        invalidate();
    }

    public float getCurrentRotation() {
        return normalizeAngle(currentRotation);
    }

    private float normalizeAngle(float angle) {
        return (angle % 360 + 360) % 360;
    }

    private void resetImage() {
        if (bitmap == null || getWidth() == 0 || cropRect.isEmpty()) return;

        matrix.reset();
        currentRotation = 0f;
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();

        float scale = Math.max(cropRect.width() / bitmapWidth, cropRect.height() / bitmapHeight);

        matrix.postScale(scale, scale);
        matrix.postTranslate(cropRect.centerX() - (bitmapWidth * scale) / 2f,
                             cropRect.centerY() - (bitmapHeight * scale) / 2f);
        checkBounds();

        if (rotationListener != null) rotationListener.onRotationChanged(0f);
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

        int iterations = 0;
        while (!isCropRectInsideBitmap() && iterations < 50) {
            matrix.postScale(1.02f, 1.02f, cropRect.centerX(), cropRect.centerY());
            iterations++;
        }

        RectF bounds = new RectF();
        calculateTransformedBitmapBounds(bounds);
        float dx = 0, dy = 0;
        
        if (bounds.left > cropRect.left) dx = cropRect.left - bounds.left;
        else if (bounds.right < cropRect.right) dx = cropRect.right - bounds.right;
        
        if (bounds.top > cropRect.top) dy = cropRect.top - bounds.top;
        else if (bounds.bottom < cropRect.bottom) dy = cropRect.bottom - bounds.bottom;

        matrix.postTranslate(dx, dy);
    }

    private void calculateTransformedBitmapBounds(RectF outBounds) {
        outBounds.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        matrix.mapRect(outBounds);
    }

    private boolean isCropRectInsideBitmap() {
        float[] bitmapPts = new float[] {
                0, 0,
                bitmap.getWidth(), 0,
                bitmap.getWidth(), bitmap.getHeight(),
                0, bitmap.getHeight()
        };
        matrix.mapPoints(bitmapPts);

        PointF[] poly = new PointF[] {
                new PointF(bitmapPts[0], bitmapPts[1]),
                new PointF(bitmapPts[2], bitmapPts[3]),
                new PointF(bitmapPts[4], bitmapPts[5]),
                new PointF(bitmapPts[6], bitmapPts[7])
        };

        return isPointInPoly(cropRect.left, cropRect.top, poly) &&
               isPointInPoly(cropRect.right, cropRect.top, poly) &&
               isPointInPoly(cropRect.right, cropRect.bottom, poly) &&
               isPointInPoly(cropRect.left, cropRect.bottom, poly);
    }

    private boolean isPointInPoly(float x, float y, PointF[] poly) {
        boolean inside = false;
        for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
            if (((poly[i].y > y) != (poly[j].y > y)) &&
                    (x < (poly[j].x - poly[i].x) * (y - poly[i].y) / (poly[j].y - poly[i].y) + poly[i].x)) {
                inside = !inside;
            }
        }
        return inside;
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

        Path path = new Path();
        path.addRect(0, 0, getWidth(), getHeight(), Path.Direction.CW);
        
        if (frameType == CropOptions.FrameType.CIRCLE) {
            path.addCircle(cropRect.centerX(), cropRect.centerY(), Math.min(cropRect.width(), cropRect.height()) / 2f, Path.Direction.CCW);
        } else {
            path.addRect(cropRect, Path.Direction.CCW);
        }
        canvas.drawPath(path, overlayPaint);

        if (frameType == CropOptions.FrameType.CIRCLE) {
            canvas.drawCircle(cropRect.centerX(), cropRect.centerY(), Math.min(cropRect.width(), cropRect.height()) / 2f, borderPaint);
        } else {
            canvas.drawRect(cropRect, borderPaint);
        }

        drawCorners(canvas);

        if (showGuides && frameType == CropOptions.FrameType.RECTANGLE) {
            float thirdWidth = cropRect.width() / 3;
            float thirdHeight = cropRect.height() / 3;

            canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, cropRect.left + thirdWidth, cropRect.bottom, guidePaint);
            canvas.drawLine(cropRect.left + 2 * thirdWidth, cropRect.top, cropRect.left + 2 * thirdWidth, cropRect.bottom, guidePaint);
            canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, cropRect.right, cropRect.top + thirdHeight, guidePaint);
            canvas.drawLine(cropRect.left, cropRect.top + 2 * thirdHeight, cropRect.right, cropRect.top + 2 * thirdHeight, guidePaint);
        }
    }

    private void drawCorners(Canvas canvas) {
        float len = 20 * getResources().getDisplayMetrics().density;

        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left + len, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.top, cropRect.left, cropRect.top + len, cornerPaint);

        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right - len, cropRect.top, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.top, cropRect.right, cropRect.top + len, cornerPaint);

        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.left + len, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.left, cropRect.bottom, cropRect.left, cropRect.bottom - len, cornerPaint);

        canvas.drawLine(cropRect.right, cropRect.bottom, cropRect.right - len, cropRect.bottom, cornerPaint);
        canvas.drawLine(cropRect.right, cropRect.bottom, cropRect.right, cropRect.bottom - len, cornerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);

        if (!rotationEnabled) {
            if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                checkBounds();
                invalidate();
            }
            return true;
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    lastAngle = calculateAngle(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    float currentAngle = calculateAngle(event);
                    float deltaAngle = currentAngle - lastAngle;
                    matrix.postRotate(deltaAngle, cropRect.centerX(), cropRect.centerY());
                    currentRotation += deltaAngle;
                    lastAngle = currentAngle;

                    if (rotationListener != null) {
                        rotationListener.onRotationChanged(getCurrentRotation());
                    }

                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                checkBounds();
                invalidate();
                break;
        }
        return true;
    }

    private float calculateAngle(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.toDegrees(Math.atan2(y, x));
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
        Bitmap result = Bitmap.createBitmap((int)cropRect.width(), (int)cropRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Matrix cropMatrix = new Matrix(matrix);
        cropMatrix.postTranslate(-cropRect.left, -cropRect.top);
        canvas.drawBitmap(bitmap, cropMatrix, new Paint(Paint.FILTER_BITMAP_FLAG));
        return result;
    }

    public void rotate(float degrees) {
        if (bitmap == null) return;
        matrix.postRotate(degrees, cropRect.centerX(), cropRect.centerY());
        currentRotation += degrees;
        checkBounds();
        invalidate();

        if (rotationListener != null) {
            rotationListener.onRotationChanged(getCurrentRotation());
        }
    }

    public void flip() {
        if (bitmap == null) return;
        matrix.postScale(-1, 1, cropRect.centerX(), cropRect.centerY());
        checkBounds();
        invalidate();
    }
}
