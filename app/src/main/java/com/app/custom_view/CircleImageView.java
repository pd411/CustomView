package com.app.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircleImageView extends View {
    private Context mContext;
    private int radius = 0;
    private int color = Color.BLACK;
    private int alpha = 100;

    private Paint circle;

    public CircleImageView(Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        init();
    }

    private void init() {
        circle = new Paint();
        circle.setAntiAlias(true);
    }

    public void setColor(int color) {
        circle.setColor(color);
        invalidate();
    }

    public void setRadius(int radius) {
        this.radius = radius;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 设置中心
        int center = getHeight() / 2;
        canvas.drawCircle(center, center, radius, circle);
    }
}
