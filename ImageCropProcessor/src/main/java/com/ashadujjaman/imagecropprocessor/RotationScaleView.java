package com.ashadujjaman.imagecropprocessor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class RotationScaleView extends View {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float currentRotation = 0f;
    private OnRotationChangeListener listener;
    private float lastX;

    public interface OnRotationChangeListener {
        void onRotationChanged(float deltaAngle);
    }

    public RotationScaleView(Context context) {
        super(context);
        init();
    }

    public RotationScaleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint.setColor(Color.LTGRAY);
        linePaint.setStrokeWidth(2f);

        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStrokeWidth(6f);
        indicatorPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setOnRotationChangeListener(OnRotationChangeListener listener) {
        this.listener = listener;
    }

    public void setRotation(float rotation) {
        // Normalize rotation to 0-360 range
        this.currentRotation = (rotation % 360 + 360) % 360;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;

        // Draw scale lines
        float step = 15f; // distance between lines
        int range = 30; // number of lines to each side

        // The offset should represent the fractional part of the rotation to make it feel smooth
        // We use a sensitivity factor to map rotation degrees to pixels
        float degreesPerStep = 2.0f;
        float offset = (currentRotation % degreesPerStep) * (step / degreesPerStep);

        for (int i = -range; i <= range; i++) {
            float x = centerX + (i * step) - offset;

            // Logic to determine which line is "main" (every 5 degrees)
            // This is a bit tricky with smooth scrolling.
            // We can approximate by checking the absolute degree value.
            float degreeAtX = currentRotation + (i * degreesPerStep) - (currentRotation % degreesPerStep);

            float lineHeight;
            if (Math.round(degreeAtX) % 10 == 0) {
                lineHeight = height * 0.7f;
                linePaint.setAlpha(255);
            } else if (Math.round(degreeAtX) % 5 == 0) {
                lineHeight = height * 0.5f;
                linePaint.setAlpha(200);
            } else {
                lineHeight = height * 0.3f;
                linePaint.setAlpha(150);
            }

            canvas.drawLine(x, height - lineHeight, x, height, linePaint);
        }

        // Draw center indicator
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStrokeWidth(8f);
        canvas.drawLine(centerX, 0, centerX, height, indicatorPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = event.getX() - lastX;
                float deltaAngle = deltaX / 10f; // sensitivity: higher divisor = slower rotation
                if (listener != null) {
                    listener.onRotationChanged(deltaAngle);
                }
                lastX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                performClick();
                break;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
