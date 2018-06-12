package com.github.tabindicator;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TabItemView extends AppCompatTextView {

    private int selectColor = Color.parseColor("#00b888");
    private int unSelectColor = Color.parseColor("#80888e");

    public TabItemView(Context context) {
        this(context, null);
    }

    public TabItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setTextColor(unSelectColor);
        setTextSize(14);
        setLines(1);
        setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        setGravity(Gravity.CENTER);
    }

    public void setSelectColor(@ColorRes int selectColor) {
        this.selectColor = ContextCompat.getColor(getContext(), selectColor);
    }

    public void setUnSelectColor(@ColorRes int unSelectColor) {
        this.unSelectColor = ContextCompat.getColor(getContext(), unSelectColor);
    }

    public void select(boolean isSelect) {
        if (isSelect) {
            setTextColor(selectColor);
        } else {
            setTextColor(unSelectColor);
        }
    }
}
