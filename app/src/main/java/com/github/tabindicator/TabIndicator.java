package com.github.tabindicator;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;

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
    public void setAdapter(TabAdapter<T> tabAdapter) {
        this.tabAdapter = tabAdapter;
    }

    // bind data
    public void setData(ArrayList<TabItem<T>> data) {
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

        int position = 0;
        for (final TabItem<T> tabItem : data) {
            final int index = position;

            View itemView = tabAdapter.onCreateTabItemView(tabItem.tabName, tabItem, position);
            if (itemView != null) {
                if (tabView.getChildCount() == 0) {
                    selectedTabItemView = itemView;
                }

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectTab(index);

                        if (onTabClickListener != null) {
                            onTabClickListener.onTabClick(v, tabItem, index);
                        }
                    }
                });
                tabView.addView(itemView);
            }

            position += 1;
        }

        tabAdapter.onSelectTab(selectedTabItemView, true);

        initTabIndicator();
    }

    private void initTabIndicator() {
        if (tabIndicator == null) {
            tabIndicator = tabAdapter.onCreateTabIndicator();
            if (tabIndicator != null) {
                container.addView(tabIndicator);
            }
        }

        if (tabIndicator != null && selectedTabItemView != null) {
            selectedTabItemView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            ViewGroup.LayoutParams layoutParams = tabIndicator.getLayoutParams();
                            if (layoutParams != null) {
                                layoutParams.width = selectedTabItemView.getWidth();
                                tabIndicator.setLayoutParams(layoutParams);
                            }

                            tabIndicator.setX(selectedTabItemView.getX());

                            selectedTabItemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
        }
    }

    public void selectTab(int position) {
        int tabCount = tabView.getChildCount();
        if (position < 0 || position >= tabCount) {
            return;
        }

        for (int i = 0; i < tabCount; i++) {
            View childView = tabView.getChildAt(i);

            boolean isSelect = i == position;
            if (isSelect) {
                selectedTabItemView = childView;
            }
            tabAdapter.onSelectTab(childView, isSelect);
        }

        startTabIndicatorAnimator();

        ViewUtils.scrollToCenterHorizontal(this, selectedTabItemView);
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

        int width = tabIndicator.getWidth();
        int selectWidth = selectedTabItemView.getWidth();
        if (width != selectWidth) {
            ValueAnimator widthAnimator = ValueAnimator.ofInt(width, selectWidth);
            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams layoutParams = tabIndicator.getLayoutParams();
                    if (layoutParams != null) {
                        layoutParams.width = (int) animation.getAnimatedValue();
                        tabIndicator.setLayoutParams(layoutParams);
                    }
                }
            });
            animatorList.add(widthAnimator);
        }

        if (!animatorList.isEmpty()) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setDuration(250);
            animatorSet.playTogether(animatorList);
            animatorSet.start();
        }
    }

}
