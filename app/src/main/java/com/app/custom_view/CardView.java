package com.app.custom_view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class CardView extends RelativeLayout {
    private Context mContext;
    private TabLayout tabView;
    private RecyclerViewPager recyclerViewPager;

    private PagerAdapter adapter;

    private String[] titleList;
    private List<Pair<String, Object>> mLists;

    private int limitSpanCount = 2;
    private int marginLeft = 30;
    private int marginTop = 20;
    private int marginRight = 30;
    private int marginBottom = 20;

    private Scroller mScroller;
    private TextView getMore;

    public CardView(Context context) {
        this(context, null);
    }

    public CardView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mScroller = new Scroller(context);

        init();
    }

    private void init() {
//        this.setOrientation(VERTICAL);

        setViewPager();
        setGetMore();
    }

    private void setGetMore() {
        getMore = new TextView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, recyclerViewPager.getId());
        getMore.setLayoutParams(params);
        getMore.setGravity(Gravity.CENTER);
        getMore.setText("上拉跳转");
        getMore.setHeight(200);
        getMore.setVisibility(GONE);

        this.addView(getMore);
    }

    public void setTitleList(String[] list) {
        this.titleList = list;

        //为布局中textview设置好相关属性
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);

        for (int i = 0; i < titleList.length; i++)
        {
            TextView textView = new TextView(mContext);
            textView.setText(titleList[i]);
            textView.setLayoutParams(layoutParams);
        }
    }


    private void setViewPager() {
        recyclerViewPager = new RecyclerViewPager(mContext);
        recyclerViewPager.setId(Integer.MAX_VALUE - 1001);

        setTabView();
        tabView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, getContentHeight() - tabView.getMeasuredHeight());
                params.addRule(RelativeLayout.BELOW, tabView.getId());
                recyclerViewPager.setLayoutParams(params);
                addView(recyclerViewPager);
                tabView.getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });

    }

    private int getContentHeight() {
        Rect outRect = new Rect();
        ((Activity)mContext).getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect);
        return outRect.height();
    }

    private int getStatusBarHeight() {
        Resources resources = mContext.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    public void setRecyclerItems(List<Pair<String, Object>> list) {
        mLists = list;
        setRecyclerView();
    }

    private void setRecyclerView() {
        List<View> views = new ArrayList<>();

        for (int i = 0; i < titleList.length; i++) {
            RecyclerView recyclerView = new CustomerRecyclerView(mContext);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(limitSpanCount, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setBackgroundColor(Color.rgb(240,240,240));
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(!recyclerView.canScrollVertically(1)){
                        Log.i("recyclerView", "direction 1: false");//滑动到底部

//                        getMore.setVisibility(VISIBLE);
                    }
                }
            });

            RecycleAdapter recycleAdapter = new RecycleAdapter(mLists);
            recyclerView.setAdapter(recycleAdapter);
            views.add(recyclerView);
        }

        adapter = new ViewAdapter(views, titleList);
        recyclerViewPager.setAdapter(adapter);
    }

    private void setTabView() {
        tabView = new TabLayout(mContext);

        tabView.setId(Integer.MAX_VALUE - 1000);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        tabView.setLayoutParams(params);
        tabView.setBackgroundColor(Color.rgb(240,240,240));

        tabView.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabView.setupWithViewPager(recyclerViewPager);

        this.addView(tabView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    private int getWindowHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    private int getWindowWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        assert wm != null;
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset()){
            recyclerViewPager.scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
        }
    }

    public class RecycleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Pair<String, Object>> mList;

        RecycleAdapter(List<Pair<String, Object>> mList) {
            this.mList = mList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Pair<String, Object> item = mList.get(position);
            holder.imageView.setImageResource((Integer) item.second);
            holder.textView.setText(item.first);
        }

        @Override
        public int getItemCount() {
            return this.mList.size();
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        private ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.recycler_text);
            imageView = itemView.findViewById(R.id.recycler_image);
        }
    }


    public class ViewAdapter extends PagerAdapter {
        private List<View> mViews;
        private String[] titles;

        ViewAdapter(List<View> mViews, String[] titles) {
            this.mViews = mViews;
            this.titles = titles;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(mViews.get(position), 0);
            return mViews.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

}
