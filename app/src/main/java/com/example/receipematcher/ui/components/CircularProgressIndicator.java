package com.example.receipematcher.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.receipematcher.R;

/**
 * Custom circular progress indicator for displaying match scores
 */
public class CircularProgressIndicator extends View {
    
    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private RectF rectF;
    
    private float progress = 0f; // 0-100
    private int progressColor;
    private int backgroundColor;
    private int textColor;
    private float strokeWidth;
    private boolean showText = true;
    
    public CircularProgressIndicator(Context context) {
        super(context);
        init(context, null);
    }
    
    public CircularProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public CircularProgressIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        // Default values
        progressColor = ContextCompat.getColor(context, R.color.primary);
        backgroundColor = ContextCompat.getColor(context, R.color.surface_variant);
        textColor = ContextCompat.getColor(context, R.color.text_primary);
        strokeWidth = dpToPx(4);
        
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressIndicator);
            progress = a.getFloat(R.styleable.CircularProgressIndicator_progress, 0f);
            progressColor = a.getColor(R.styleable.CircularProgressIndicator_progressColor, progressColor);
            backgroundColor = a.getColor(R.styleable.CircularProgressIndicator_backgroundColor, backgroundColor);
            textColor = a.getColor(R.styleable.CircularProgressIndicator_textColor, textColor);
            strokeWidth = a.getDimension(R.styleable.CircularProgressIndicator_strokeWidth, strokeWidth);
            showText = a.getBoolean(R.styleable.CircularProgressIndicator_showText, true);
            a.recycle();
        }
        
        setupPaints();
    }
    
    private void setupPaints() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);
        
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dpToPx(12));
        textPaint.setFakeBoldText(true);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float padding = strokeWidth / 2;
        rectF = new RectF(padding, padding, w - padding, h - padding);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (rectF == null) return;
        
        // Draw background circle
        canvas.drawOval(rectF, backgroundPaint);
        
        // Draw progress arc
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);
        
        // Draw text
        if (showText) {
            String text = Math.round(progress) + "%";
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);
            canvas.drawText(text, centerX, centerY, textPaint);
        }
    }
    
    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        
        // Update color based on progress
        if (progress >= 80) {
            progressColor = ContextCompat.getColor(getContext(), R.color.success);
        } else if (progress >= 50) {
            progressColor = ContextCompat.getColor(getContext(), R.color.primary);
        } else {
            progressColor = ContextCompat.getColor(getContext(), R.color.warning);
        }
        
        progressPaint.setColor(progressColor);
        invalidate();
    }
    
    public float getProgress() {
        return progress;
    }
    
    public void setProgressColor(int color) {
        this.progressColor = color;
        progressPaint.setColor(color);
        invalidate();
    }
    
    public void setShowText(boolean showText) {
        this.showText = showText;
        invalidate();
    }
    
    private float dpToPx(float dp) {
        return dp * getContext().getResources().getDisplayMetrics().density;
    }
}