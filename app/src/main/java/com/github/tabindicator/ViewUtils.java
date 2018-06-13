package com.github.tabindicator;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;

public class ViewUtils {

    private ViewUtils() {
    }

    public static int dip2px(Context context, float dipValue) {
        if (context == null) {
            return (int) dipValue;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static void scrollToCenterHorizontal(HorizontalScrollView scrollView, View child) {
        if (scrollView == null || child == null) {
            return;
        }

        int leftDistance = child.getLeft();
        int tabWidth = child.getWidth();
        int screenWidth = getScreenWidthPixels(scrollView.getContext());
        int distanceShouldScrollTo = leftDistance + tabWidth / 2 - screenWidth / 2;
        if (distanceShouldScrollTo > 0) {
            scrollView.smoothScrollTo(distanceShouldScrollTo, 0);
        } else {
            scrollView.smoothScrollTo(0, 0);
        }
    }

    private static int getScreenWidthPixels(Context context) {
        if (context == null) {
            return 0;
        }

        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(dm);
            return dm.widthPixels;
        }
        return 0;
    }
}
