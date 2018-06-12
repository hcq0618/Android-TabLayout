package com.github.tabindicator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TabIndicator<T> extends HorizontalScrollView {

    private final FrameLayout container;

    private LinearLayout tabView;

    private OnTabClickListener<T> onTabClickListener;

    private TabAdapter<T> tabAdapter;

    private View tabIndicator;

    private View selectedTabItemView;

    public TabIndicator(Context context) {
        this(context, null);
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int height = ViewUtils.dip2px(context, 44);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        setHorizontalScrollBarEnabled(false);
        setHorizontalFadingEdgeEnabled(false);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setFillViewport(true);

        container = new FrameLayout(context);
        addView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
    }

    // custom ui style
    public void setTabAdapter(TabAdapter<T> tabAdapter) {
        this.tabAdapter = tabAdapter;
    }

    // bind data   <title,data>
    public void setData(LinkedHashMap<String, T> data) {
        if (data == null) {
            return;
        }

        Context context = getContext();

        if (tabView == null) {
            if (tabAdapter == null) {
                tabAdapter = new DefaultTabAdapter<>(context);
            }
            tabView = tabAdapter.onCreateTabView();
            container.addView(tabView, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            tabView.removeAllViews();
        }

        if (tabView == null) {
            return;
        }

        Set<Map.Entry<String, T>> entrySet = data.entrySet();
        int position = 0;
        for (Map.Entry<String, T> entry : entrySet) {
            final int index = position;
            String tabName = entry.getKey();
            final T tabData = entry.getValue();

            View itemView = tabAdapter.onCreateTabItemView(tabName, tabData, position);
            if (itemView != null) {
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectTab(index);

                        if (onTabClickListener != null) {
                            onTabClickListener.onTabClick(v, tabData, index);
                        }
                    }
                });
                tabView.addView(itemView);

                if (selectedTabItemView == null) {
                    selectedTabItemView = itemView;
                }
            }

            position += 1;
        }

        resetTabIndicator();
    }

    private void resetTabIndicator() {
        if (tabIndicator == null) {
            tabIndicator = tabAdapter.onCreateTabIndicator();
            if (tabIndicator != null) {
                container.addView(tabIndicator);
            }
        }

        if (tabIndicator != null && selectedTabItemView != null) {
            tabIndicator.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            tabIndicator.setLeft(selectedTabItemView.getLeft());
                            tabIndicator.setRight(selectedTabItemView.getRight());

                            tabAdapter.onSelectTab(selectedTabItemView, true);

                            tabIndicator.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }
    }

    public void selectTab(int position) {
        int tabCount = tabView.getChildCount();
        for (int i = 0; i < tabCount; i++) {
            View childView = tabView.getChildAt(i);

            boolean isSelect = i == position;
            if (isSelect) {
                selectedTabItemView = childView;
            }
            tabAdapter.onSelectTab(childView, isSelect);
        }

        startTabIndicatorAnimator();

        scrollToCenterHorizontal(selectedTabItemView);
    }

    private void scrollToCenterHorizontal(View child) {
        int leftDistance = child.getLeft();
        int tabWidth = child.getWidth();
        int screenWidth = getScreenWidthPixels();
        int distanceShouldScrollTo = leftDistance + tabWidth / 2 - screenWidth / 2;
        if (distanceShouldScrollTo > 0) {
            smoothScrollTo(distanceShouldScrollTo, 0);
        } else {
            smoothScrollTo(0, 0);
        }
    }

    private int getScreenWidthPixels() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
            return dm.widthPixels;
        }
        return 0;
    }

    public void setOnTabClickListener(OnTabClickListener<T> clickListener) {
        onTabClickListener = clickListener;
    }

    private void startTabIndicatorAnimator() {
        if (tabIndicator == null || selectedTabItemView == null) {
            return;
        }

        ArrayList<Animator> animatorList = new ArrayList<>();

        float x = tabIndicator.getX();
        float selectX = selectedTabItemView.getX();
        if (x != selectX) {
            ValueAnimator translationAnimator = ValueAnimator.ofFloat(x, selectX);
            translationAnimator.setDuration(500);
            translationAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            tabIndicator.setX((float) animation.getAnimatedValue());
                        }
                    }
            );
            animatorList.add(translationAnimator);
        }

        final ViewGroup.LayoutParams layoutParams = tabIndicator.getLayoutParams();
        if (layoutParams != null) {
            int width = layoutParams.width;
            int selectWidth = selectedTabItemView.getWidth();
            if (width != selectWidth) {
                ValueAnimator widthAnimator = ValueAnimator.ofInt(width, selectWidth);
                widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        layoutParams.width = (int) animation.getAnimatedValue();
                        tabIndicator.setLayoutParams(layoutParams);
                    }
                });
                animatorList.add(widthAnimator);
            }
        }

        if (!animatorList.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(500);
            animatorSet.playTogether(animatorList);
            animatorSet.start();
        }
    }

}
