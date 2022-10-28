/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import java.text.DecimalFormat;

public class SizeUtils {

    /**
     * Conversion of DP to PX units
     *
     * @param context context object ApplicationContext
     * @param dp       DP value
     * @return PX value
     */
    public static int dp2Px(Context context, float dp) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        float scale = displayMetrics.density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * Obtains the pixel value of the screen width.
     *
     * @param context Context object ApplicationContext
     * @return Width px
     */
    public static int screenWidth(Context context) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        return displayMetrics.widthPixels;
    }

    private static final long GB = 1024L * 1024L * 1024L;

    private static final long MB = 1024L * 1024L;

    private static final long KB = 1024L;

    public static String bytes2kb(long bytes) {
        DecimalFormat format = new DecimalFormat("###.02");
        if (bytes / GB >= 1) {
            return format.format((bytes / GB)) + "GB";
        } else if (bytes / MB >= 1) {
            return format.format((bytes / MB)) + "MB";
        } else if (bytes / KB >= 1) {
            return format.format((bytes / KB)) + "KB";
        } else {
            return bytes + "B";
        }
    }
}
