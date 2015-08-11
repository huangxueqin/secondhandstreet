package com.example.secondhandstreet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by huangxueqin on 15-4-12.
 */
public class TabIndicatorLayout extends ViewGroup{
    public TabIndicatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIndicatorLayout(Context context) {
        this(context, null);
    }

    public TabIndicatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void switchToTabOfType(int type) {
        int count = getChildCount();
        for(int i = 0; i < count; i++) {
            TabIndicator child = (TabIndicator) getChildAt(i);
            child.setChecked(child.getType() == type);
        }
    }

    public void onTabClicked(View v) {
        for(int i = 0; i < getChildCount(); i++) {
            TabIndicator child = (TabIndicator)getChildAt(i);
            if(child == v) {
                child.setChecked(true);
            }
            else {
                child.setChecked(false);
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int vpadding = getPaddingBottom() + getPaddingTop();
        int hPadding = getPaddingLeft() + getPaddingRight();
        int count = getChildCount();
        if(count == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
        else {
            int maxChildWidth = (width - hPadding) / count;
            int maxChildHeight = (height - vpadding);
            int maxRealChildHeight = 0;
            int maxRealChildWidth = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                child.measure(MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(maxChildHeight, MeasureSpec.AT_MOST));
                maxRealChildHeight = Math.max(maxRealChildHeight, child.getMeasuredHeight());
                maxRealChildWidth = Math.max(maxRealChildWidth, child.getMeasuredWidth());
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, maxRealChildWidth * count + hPadding);
            }
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, maxRealChildHeight + vpadding);
            }
            this.setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        if(count == 0) {
            return;
        }
        int hstride = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / count;
        int vstride = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            int hOffset = (hstride - cw) / 2;
            int vOffset = (vstride - ch) / 2;
            child.layout(left+hOffset, top+vOffset, left+hOffset+cw, top+vOffset+ch);
            left += hstride;
        }
    }
}
