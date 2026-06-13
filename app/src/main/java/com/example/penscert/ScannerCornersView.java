package com.example.penscert;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view that draws premium glowing corner brackets for the scanner frame.
 */
public class ScannerCornersView extends View {
    private Paint paint;
    private Paint glowPaint;
    private float cornerLength;
    private float thickness;
    private float cornerRadius;

    public ScannerCornersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        cornerLength = 40 * density;
        thickness = 3.5f * density;
        cornerRadius = 6 * density;

        // Main corner paint with gradient blue
        paint = new Paint();
        paint.setStrokeWidth(thickness);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        paint.setColor(0xFF818CF8); // primary_light indigo

        // Glow layer paint
        glowPaint = new Paint();
        glowPaint.setStrokeWidth(thickness * 2.5f);
        glowPaint.setStyle(Paint.Style.STROKE);
        glowPaint.setStrokeCap(Paint.Cap.ROUND);
        glowPaint.setAntiAlias(true);
        glowPaint.setColor(0x40818CF8); // soft glow indigo
        try {
            glowPaint.setMaskFilter(new BlurMaskFilter(8 * density, BlurMaskFilter.Blur.NORMAL));
        } catch (Exception ignored) {}

        // Required for MaskFilter to work
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float r = cornerRadius;
        float cl = cornerLength;

        // Draw glow layer first
        drawCorners(canvas, w, h, r, cl, glowPaint);
        // Draw crisp corners on top
        drawCorners(canvas, w, h, r, cl, paint);
    }

    private void drawCorners(Canvas canvas, int w, int h, float r, float cl, Paint p) {
        // Top-left
        canvas.drawArc(0, 0, r * 2, r * 2, 180, 90, false, p);
        canvas.drawLine(r, 0, cl, 0, p);
        canvas.drawLine(0, r, 0, cl, p);

        // Top-right
        canvas.drawArc(w - r * 2, 0, w, r * 2, 270, 90, false, p);
        canvas.drawLine(w - cl, 0, w - r, 0, p);
        canvas.drawLine(w, r, w, cl, p);

        // Bottom-left
        canvas.drawArc(0, h - r * 2, r * 2, h, 90, 90, false, p);
        canvas.drawLine(r, h, cl, h, p);
        canvas.drawLine(0, h - cl, 0, h - r, p);

        // Bottom-right
        canvas.drawArc(w - r * 2, h - r * 2, w, h, 0, 90, false, p);
        canvas.drawLine(w - cl, h, w - r, h, p);
        canvas.drawLine(w, h - cl, w, h - r, p);
    }
}
