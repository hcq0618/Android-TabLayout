package com.github.tabindicator;

import android.view.View;
import android.widget.LinearLayout;

public interface TabAdapter<T> {
    LinearLayout onCreateTabView();

    View onCreateTabItemView(String title, TabItem<T> itemData, int position);

    View onCreateTabIndicator();

    void onSelectTab(View tabItemView, boolean isSelect);
}
