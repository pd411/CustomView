package com.app.custom_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * BannerViewGroup由一个GridView和ViewPager组成
 */
public class BannerViewGroup extends LinearLayout {
    private Context mContext;
    private int lines = 1;
    private int columns = 5;
    private List<Pair<String, Object>> items;
    private int limitFirst = 10;
    private int totalHeight;

    private ViewPager pager;
    private LinearLayout points;

    private int pointSize = 100;

    private List<View> mView = new ArrayList<>();
    private CircleImageView[] pImages;
    private MyViewPagerAdapter adapter;

    public BannerViewGroup(Context context) {
        this(context, null);
    }

    public BannerViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BannerViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerViewGroup);
        lines = typedArray.getInteger(R.styleable.BannerViewGroup_banner_lines, lines);
        columns = typedArray.getInteger(R.styleable.BannerViewGroup_banner_columns, columns);

        this.setOrientation(VERTICAL);

        init();
    }

    private void init() {
        // 添加滑动pager
        setPager();

        // 添加小圆点
        setPoints();

    }

    private void setPager() {
        pager = new ViewPager(mContext);
        this.addView(pager);
    }

    private void setPoints() {
        points = new LinearLayout(mContext);
        LayoutParams pointParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        points.setLayoutParams(pointParams);
        points.setOrientation(LinearLayout.HORIZONTAL);
        points.setGravity(Gravity.CENTER);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < pImages.length; i++) {
                    if (i == position) pImages[i].setColor(Color.RED);
                    else pImages[i].setColor(Color.GRAY);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        this.addView(points);
    }

    public void setItems(List<Pair<String, Object>> items) {
        this.items = items;
        setGridView();
        pager.setAdapter(adapter);
        pager.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, totalHeight));
    }

    /**
     *  初始化ViewPager内部GridView
     */
    private void setGridView() {
        int index = 0;

        // gridView 填充数据
        while (index < items.size()) {
            GridView gridView = new GridView(mContext);
            gridView.setNumColumns(columns);
            gridView.setColumnWidth(GridView.AUTO_FIT);

            List<Pair<String, Object>> fill = new ArrayList<>();
            int cur = index;
            for (int i = cur; i < cur + limitFirst; i++) {
                if (index >= items.size()) break;
                fill.add(new Pair<>(items.get(i).first, items.get(i).second));
                index++;
            }
            GridAdapter adapter = new GridAdapter(fill);
            gridView.setAdapter(adapter);

            // 获取listview的每一个item
            View listItem = adapter.getView(0, null, gridView);
            listItem.measure(0, 0);
            // 获取item的高度和
            totalHeight = (int) (listItem.getMeasuredHeight() * Math.ceil(limitFirst / (columns * 1.0)));
            Log.d("totalHeight", totalHeight + "");

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(mContext, items.get(position).first, Toast.LENGTH_SHORT).show();
                }
            });

            mView.add(gridView);
        }

        this.adapter = new MyViewPagerAdapter(mView);

        pImages = new CircleImageView[mView.size()];

        for (int i = 0; i < mView.size(); i++) {
            // 添加圆点
            CircleImageView imageView = new CircleImageView(mContext);
            imageView.setLayoutParams(new LayoutParams(pointSize, pointSize));

//            if (i == 0) imageView.setBackgroundColor(Color.RED);
//            else imageView.setBackgroundColor(Color.GRAY);

            imageView.setRadius(10);

            if (i == 0) imageView.setColor(Color.RED);
            else imageView.setColor(Color.GRAY);

            pImages[i] = imageView;
            points.addView(pImages[i]);
        }

    }

    /**
     * GridView的适配器
     */
    private class GridAdapter extends BaseAdapter {
        List<Pair<String, Object>> mData;

        public GridAdapter(List<Pair<String, Object>> mData) {
            this.mData = mData;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflate(mContext, R.layout.grid_item, null);
            ImageView imageView = convertView.findViewById(R.id.grid_item_image);
            TextView textView = convertView.findViewById(R.id.grid_item_text);

            imageView.setImageResource((Integer) mData.get(position).second);
            textView.setText(mData.get(position).first);
            return convertView;
        }
    }

    /**
     * ViewPager的适配器
     */
    private class MyViewPagerAdapter extends PagerAdapter {
        private List<View> views;

        public MyViewPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(views.get(position), 0);
            return views.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}
