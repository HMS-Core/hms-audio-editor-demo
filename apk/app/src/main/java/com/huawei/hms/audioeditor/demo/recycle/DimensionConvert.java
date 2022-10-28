/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.recycle;

import android.content.Context;

/**
 * Page resolution conversion
 *
 */
public class DimensionConvert {
    /**
     * The unit is converted from dp to px based on the resolution of the phone.
     *
     * @param context context
     * @param dpValue dp value to convert
     * @return converted value
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * The unit is changed from px (pixel) to dp based on the resolution of the phone.
     *
     * @param context context
     * @param pxValue dx value to convert
     * @return converted value
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
