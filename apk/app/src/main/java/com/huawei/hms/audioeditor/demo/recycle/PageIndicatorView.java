/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.recycle;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.huawei.hms.audioeditor.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Display class of each subbutton on the home page.
 *
 */
public class PageIndicatorView extends LinearLayout {
    private Context mContext = null;

    private int dotSize = 6;

    private List<View> indicatorViews = null;

    private int margins = 4;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attributeSet, int style) {
        super(context, attributeSet, style);
        init(context);
    }

    public PageIndicatorView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    private void init(Context context) {
        this.mContext = context;

        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        margins = DimensionConvert.dip2px(context, margins);
        dotSize = DimensionConvert.dip2px(context, dotSize);
    }

    public void initIndicator(int allCount) {
        if (indicatorViews == null) {
            indicatorViews = new ArrayList<>();
        } else {
            indicatorViews.clear();
            removeAllViews();
        }
        View tmpView;
        LayoutParams params = new LayoutParams(dotSize, dotSize);
        params.setMargins(margins, margins, margins, margins);
        for (int i = 0; i < allCount; i++) {
            tmpView = new View(mContext);
            tmpView.setBackgroundResource(R.drawable.bg_page_indicator_tm);
            addView(tmpView, params);
            indicatorViews.add(tmpView);
        }
        if (indicatorViews.size() > 0) {
            indicatorViews.get(0).setBackgroundResource(R.drawable.bg_page_indicator);
        }
    }

    /**
     * Set Selected Page
     *
     * @param selected select index
     */
    public void setSelectedPage(int selected) {
        for (int i = 0; i < indicatorViews.size(); i++) {
            if (i == selected) {
                indicatorViews.get(i).setBackgroundResource(R.drawable.bg_page_indicator);
            } else {
                indicatorViews.get(i).setBackgroundResource(R.drawable.bg_page_indicator_tm);
            }
        }
    }
}
