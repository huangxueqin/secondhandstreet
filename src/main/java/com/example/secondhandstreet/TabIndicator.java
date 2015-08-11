package com.example.secondhandstreet;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by huangxueqin on 15-4-12.
 */
public class TabIndicator extends ImageView{

    private final static int[] STATE_CHECKED = { R.attr.state_checked };
    private boolean mChecked = false;
    private int mType = 0;

    public TabIndicator(Context context) {
        this(context, null);
    }

    public TabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        refreshDrawableState();
    }

    public void setType(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        if(mChecked) {
            final int[] drawableState = super.onCreateDrawableState(extraSpace + STATE_CHECKED.length);
            mergeDrawableStates(drawableState, STATE_CHECKED);
            return drawableState;
        }
        return super.onCreateDrawableState(extraSpace);
    }
}
