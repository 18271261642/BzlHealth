package com.bozlun.health.android.b30.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

/**
 * Created by Admin
 * Date 2019/1/17
 */
public class CusExpandableListView extends ExpandableListView {
    public CusExpandableListView(Context context) {
        super(context);
    }

    public CusExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
