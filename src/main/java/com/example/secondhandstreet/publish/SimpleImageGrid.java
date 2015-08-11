package com.example.secondhandstreet.publish;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Adapter;

import com.example.secondhandstreet.R;
import com.example.secondhandstreet.Utils.LogUtil;

import java.util.List;

/**
 * Created by huangxueqin on 15-3-15.
 */
public class SimpleImageGrid extends ViewGroup{
    private static final String TAG = "HXQ_CellLayout TAG";

    private static final int COLUMN_NUM = 4;

    private int mVerticalSpacing;
    private int mHorizontalSpacing;
    private int mCellWidth;
    private int mCellHeight;
    private int mColumnNum;
    private int mRowNum;

    private Adapter mAdapter;

    private final DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            refreshViewsFromAdapter();
        }
        @Override
        public void onInvalidated() {
            removeAllViews();
        }
    };

    public SimpleImageGrid(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SimpleImageGrid);
        mColumnNum = a.getInt(R.styleable.SimpleImageGrid_column_num, COLUMN_NUM);
        mVerticalSpacing = a.getDimensionPixelSize(R.styleable.SimpleImageGrid_vertical_spacing, 0);
        mHorizontalSpacing = a.getDimensionPixelSize(R.styleable.SimpleImageGrid_horizontal_spacing, 0);

        ViewTreeObserver viewTreeObserver = this.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    LogUtil.logd("onGlobalLayout running");
                    SimpleImageGrid.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if(mAdapter != null) {
                        initViewFromAdapter();
                    }
                }
            });
        }
    }

    public SimpleImageGrid(Context context) {
        this(context, null);
    }

    public SimpleImageGrid(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int top = getPaddingTop();
        for(int i = 0; i < mRowNum; i++) {
            int left = getPaddingLeft();
            for(int j = 0; j < mColumnNum; j++) {
                if(i*mColumnNum + j >= count) {
                    break;
                }
                View child = getChildAt(i*mColumnNum + j);
                int leftOffset = (mCellWidth - child.getMeasuredWidth()) / 2;
                int topOffset = (mCellHeight - child.getMeasuredHeight()) / 2;
                child.layout(left + leftOffset, top + topOffset,
                        left + leftOffset + child.getMeasuredWidth(),
                        top + topOffset + child.getMeasuredHeight());
                left += mCellWidth + mHorizontalSpacing;
            }
            top += mCellHeight + mVerticalSpacing;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        LogUtil.logd("onMeasure");
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int horizontalPadding = getPaddingLeft() + getPaddingRight();
        int verticalPadding = getPaddingTop() + getPaddingBottom();

        mCellWidth = (widthSpecSize - horizontalPadding - (mColumnNum-1)*mHorizontalSpacing) / mColumnNum;
        mCellHeight = mCellWidth;

        int count = getChildCount();
        mRowNum = (int) Math.ceil(((double)count) / mColumnNum);
        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY));
        }
        heightSpecSize = verticalPadding + mCellHeight * mRowNum + (mRowNum-1)*verticalPadding;

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if(mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        if(mAdapter != null) {
            mAdapter.registerDataSetObserver(mObserver);
        }
        if(mCellHeight > 0 && mCellWidth > 0) {
            initViewFromAdapter();
        }
    }

    private void initViewFromAdapter() {
        LogUtil.logd("initViewFromAdapter");
        removeAllViews();
        if(mAdapter != null) {
            for(int i = 0; i < mAdapter.getCount(); i++) {
                addView(mAdapter.getView(i, null, this), i);
            }
        }
    }

    private void refreshViewsFromAdapter() {
        int childCount = getChildCount();
        int adapterSize = mAdapter.getCount();
        int reuseCount = Math.min(childCount, adapterSize);

        for (int i = 0; i < reuseCount; i++) {
            mAdapter.getView(i, getChildAt(i), this);
        }

        if (childCount < adapterSize) {
            for (int i = childCount; i < adapterSize; i++) {
                addView(mAdapter.getView(i, null, this), i);
            }
        } else if (childCount > adapterSize) {
            removeViews(adapterSize, childCount-adapterSize);
        }
    }
}
