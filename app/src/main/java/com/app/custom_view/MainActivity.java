package com.app.custom_view;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private StepView stepView;
    private BannerViewGroup bannerView;
    private CardView cardView;
    private ScrollableView scrollView;

    private String[] bannerText = {
            "美食", "电影/演出", "酒店/门票", "休闲娱乐", "外卖", "每日福利", "霸王餐", "年度大赏", "丽人/美发", "商场/商圈"
            ,"美食", "电影/演出", "酒店/门票", "休闲娱乐", "外卖", "每日福利", "霸王餐", "年度大赏", "丽人/美发", "商场/商圈"
    };
    private int[] bannerImage = {
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground

            ,
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground, R.drawable.ic_launcher_foreground,
            R.drawable.ic_launcher_foreground
    };
    private List<Pair<String, Object>> bannerItems;

    private String[] cardText = {
            "推荐", "附近", "大人探店", "菜谱", "遛娃", "视频", "玩乐", "旅行", "电影", "美食"
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bannerView = findViewById(R.id.banner);
        cardView = findViewById(R.id.card_view);
        scrollView = findViewById(R.id.scroll_view);

        cardView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.setLimitTop(cardView.getTop());
            }
        });

        setBannerView();
        setCardView();
        setScrollView();
    }

    private void setScrollView() {
    }

    private void setCardView() {
        cardView.setTitleList(cardText);

        List<Pair<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < bannerText.length; i++) {
            Pair<String, Object> pair = new Pair<String, Object>(bannerText[i], bannerImage[i]);
            list.add(pair);
        }

        cardView.setRecyclerItems(list);
    }

    private void setBannerView() {
        bannerItems = new ArrayList<>();
        for (int i = 0; i < bannerText.length; i++) {
            Pair<String, Object> pair = new Pair<String, Object>(bannerText[i], bannerImage[i]);
            bannerItems.add(pair);
        }
        bannerView.setItems(bannerItems);
    }

    private void setStepView() {
        stepView.setMaxStep(5000);
        ValueAnimator animator = ValueAnimator.ofInt(0, 3000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentStep = (int) animation.getAnimatedValue();
                stepView.setCurrentStep(currentStep);
            }
        });

        animator.setDuration(3000);
        animator.start();
    }
}
