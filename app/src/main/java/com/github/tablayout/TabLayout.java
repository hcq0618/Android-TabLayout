package com.github.tablayout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import java.util.ArrayList;

public class TabLayout<T> extends HorizontalScrollView {

    private final FrameLayout container;

    private ViewGroup tabView;

    private OnTabListener<T> onTabListener;

    private TabAdapter<T> tabAdapter;

    private View tabIndicator;

    private View selectedTabItemView;

    private ArrayList<TabItem<T>> data;

    private long indicatorAnimationDuration = 250;

    private boolean needIndicator = true;

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getLayoutHeight()));
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

    protected int getLayoutHeight() {
        return ViewUtils.dip2px(getContext(), 44);
    }

    public void setNeedIndicator(boolean needIndicator) {
        this.needIndicator = needIndicator;
    }

    // custom ui style
    public void setAdapter(TabAdapter<T> tabAdapter) {
        this.tabAdapter = tabAdapter;
    }

    public void setData(ArrayList<TabItem<T>> data) {
        setData(data, null);
    }

    // bind data
    public void setData(ArrayList<TabItem<T>> data, String selectedName) {
        this.data = data;

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

        if (data == null) {
            return;
        }

        View selectedTabItemView = null;

        int position = 0;
        for (final TabItem<T> tabItem : data) {
            final int index = position;

            View itemView = tabAdapter.onCreateTabItemView(tabItem.tabName, tabItem, position);
            if (itemView != null) {
                if (TextUtils.equals(selectedName, tabItem.tabName)) {
                    selectedTabItemView = itemView;
                }

                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectTab(index);

                        if (onTabListener != null) {
                            onTabListener.onTabClick(v, tabItem, index);
                        }
                    }
                });
                tabView.addView(itemView);
            }

            position += 1;
        }

        if (selectedTabItemView == null) {
            if (tabView.getChildCount() > 0) {
                selectedTabItemView = tabView.getChildAt(0);
            }
        }

        this.selectedTabItemView = selectedTabItemView;

        tabAdapter.onSelectTab(selectedTabItemView, true);
        if (onTabListener != null) {
            onTabListener.onTabSelected(selectedTabItemView, getSelectedItemData(),
                    tabView.indexOfChild(selectedTabItemView));
        }

        if (needIndicator) {
            initTabIndicator();
        }
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
                            int tabIndicatorWidth = getTabIndicatorWidth();
                            if (tabIndicatorWidth == 0) {
                                return;
                            }

                            ViewGroup.LayoutParams layoutParams = tabIndicator.getLayoutParams();
                            if (layoutParams != null) {
                                layoutParams.width = tabIndicatorWidth;
                                tabIndicator.setLayoutParams(layoutParams);
                            }

                            selectedTabItemView.getViewTreeObserver().removeOnGlobalLayoutListener(
                                    this);
                        }
                    });
            selectedTabItemView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int tabIndicatorWidth = getTabIndicatorWidth();
                    tabIndicator.setX(getTabIndicatorX(selectedTabItemView, tabIndicatorWidth));

                    selectedTabItemView.removeOnLayoutChangeListener(this);
                }
            });
        }
    }

    public void setIndicatorAnimationDuration(long indicatorAnimationDuration) {
        this.indicatorAnimationDuration = indicatorAnimationDuration;
    }

    public int getTabPosition(View tabItemView) {
        return tabView.indexOfChild(tabItemView);
    }

    public View getTabItemView(int position) {
        return tabView.getChildAt(position);
    }

    public View getSelectedTabItemView() {
        return selectedTabItemView;
    }

    public T getSelectedItemData() {
        int position = getTabPosition(selectedTabItemView);
        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (i == position) {
                return data.get(i).tabData;
            }
        }
        return null;
    }

    public void selectTab(int position) {
        int tabCount = tabView.getChildCount();
        if (position < 0 || position >= tabCount) {
            return;
        }

        for (int i = 0; i < tabCount; i++) {
            View childView = tabView.getChildAt(i);

            boolean isSelect = i == position;

            tabAdapter.onSelectTab(childView, isSelect);
            if (isSelect) {
                selectedTabItemView = childView;
                if (onTabListener != null) {
                    onTabListener.onTabSelected(selectedTabItemView, getSelectedItemData(), i);
                }
            }
        }

        startTabIndicatorAnimator();
    }

    public void setOnTabListener(OnTabListener<T> clickListener) {
        onTabListener = clickListener;
    }

    protected int getTabIndicatorWidth() {
        return getTabItemViewWidth(selectedTabItemView);
    }

    protected int getTabIndicatorX(View tabItemView, int tabIndicatorWidth) {
        return (int) (tabItemView.getX() + (tabItemView.getWidth() - tabIndicatorWidth) / 2);
    }

    protected int getTabItemViewWidth(View tabItemView) {
        return tabAdapter.getTabItemViewWidth(tabItemView);
    }

    protected void startTabIndicatorAnimator() {
        if (tabIndicator == null || selectedTabItemView == null || !needIndicator) {
            return;
        }

        ArrayList<Animator> animatorList = new ArrayList<>();

        int tabIndicatorWidth = getTabIndicatorWidth();

        float x = tabIndicator.getX();
        float tabIndicatorX = getTabIndicatorX(selectedTabItemView, tabIndicatorWidth);
        if (x != tabIndicatorX) {
            ValueAnimator translationAnimator = ValueAnimator.ofFloat(x, tabIndicatorX);
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

        //宽度变化动效
        int width = tabIndicator.getWidth();
        if (width != tabIndicatorWidth) {
            ValueAnimator widthAnimator = ValueAnimator.ofInt(width, tabIndicatorWidth);
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
            animatorSet.setInterpolator(new LinearInterpolator());
            animatorSet.setDuration(indicatorAnimationDuration);
            animatorSet.playTogether(animatorList);
            animatorSet.start();
        }

        ViewUtils.scrollToCenterHorizontal(this, selectedTabItemView);
    }

}
