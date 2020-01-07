package com.app.custom_view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class RecyclerViewPager extends ViewPager {
    private Context mContext;

    public RecyclerViewPager(@NonNull Context context) {
        this(context, null);
    }

    public RecyclerViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        init();
    }

    private void init() {
    }

}
