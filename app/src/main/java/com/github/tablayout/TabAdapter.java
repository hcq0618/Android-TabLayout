package com.github.tablayout;

import android.view.View;
import android.view.ViewGroup;

public interface TabAdapter<T> {
    ViewGroup onCreateTabView();

    View onCreateTabItemView(String title, TabItem<T> itemData, int position);

    View onCreateTabIndicator();

    void onSelectTab(View tabItemView, boolean isSelect);

    int getTabItemViewWidth(View tabItemView);
}
