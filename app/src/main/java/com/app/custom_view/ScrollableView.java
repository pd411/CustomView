package com.app.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ScrollableView extends ScrollView {
    private int limitTop = 0;
    private boolean isDispatch = true;
    private int startX = 0, startY = 0;

    public ScrollableView(Context context) {
        this(context, null);
    }

    public ScrollableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setLimitTop(int top) {
        this.limitTop = top;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        isDispatch = t < limitTop;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isDispatch)
            return super.onInterceptTouchEvent(ev);

        switch (ev.getAction()) {
            case  MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(ev.getX() - startX) < Math.abs(ev.getY() - startY))
                    return true;
            case MotionEvent.ACTION_UP:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
