package com.app.custom_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class StepView extends View {
    private int measure, draw = 0;
    private int maxStep = 8000;
    private int currentStep;
    private int textSize = 100;
    // 内圆
    private int innerColor = Color.BLUE;
    // 外圆
    private int outerColor = Color.RED;
    // 宽度
    private int borderSize = 100;
    private Paint innerPaint, outerPaint, stepPaint;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StepView);
        innerColor = ta.getColor(R.styleable.StepView_innerColor, innerColor);
        outerColor = ta.getColor(R.styleable.StepView_outerColor, outerColor);
        borderSize = ta.getDimensionPixelSize(R.styleable.StepView_borderSize, borderSize);
        ta.recycle();

        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);
        innerPaint.setStrokeCap(Paint.Cap.ROUND);
        innerPaint.setColor(innerColor);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setStrokeWidth(borderSize);

        outerPaint = new Paint();
        outerPaint.setAntiAlias(true);
        outerPaint.setStrokeCap(Paint.Cap.ROUND);
        outerPaint.setColor(outerColor);
        outerPaint.setStyle(Paint.Style.STROKE);
        outerPaint.setStrokeWidth(borderSize);

        stepPaint = new Paint();
        stepPaint.setAntiAlias(true);
        stepPaint.setTextSize(textSize);
        stepPaint.setTextAlign(Paint.Align.CENTER);
    }

    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw++;
        Log.d("onDraw", String.valueOf(draw));
        // 设置中心
        int center = getWidth() / 2;
        // 设置半径
        int radius = getWidth() / 2 - borderSize;
        RectF rectF = new RectF(center - radius,
                center - radius,
                center + radius,
                center + radius);
        // 画内圆
        canvas.drawArc(rectF, 0, -180, false, innerPaint);
        // 画外圆
        canvas.drawArc(rectF, -180, (float) (180 * (currentStep*1.0/maxStep)), false, outerPaint);
        // 显示文字
        canvas.drawText(String.valueOf(currentStep), getWidth()/2, getHeight()/2, stepPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measure++;
        Log.d("onMeasure", String.valueOf(measure));

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int result = Math.min(widthSize, heightSize);

        // 设置图片的为正方形
        setMeasuredDimension(result, result);
    }
}
