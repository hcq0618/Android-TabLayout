package com.github.tabindicator;

import android.view.View;

public interface OnTabClickListener<T> {
    void onTabClick(View tabItemView, T itemData, int position);
}
