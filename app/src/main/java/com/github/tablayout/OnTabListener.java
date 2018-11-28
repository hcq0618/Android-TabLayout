package com.github.tablayout;

import android.view.View;

public interface OnTabListener<T> {
    void onTabClick(View tabItemView, TabItem<T> itemData, int position);

    void onTabSelected(View tabItemView, T itemData, int position);
}
