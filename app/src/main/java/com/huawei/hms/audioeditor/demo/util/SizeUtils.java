/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;


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
     * PX to DP unit conversion
     *
     * @param context Context object ApplicationContext
     * @param px      PX Value
     * @return DP Value
     */
    public static int px2Dp(Context context, float px) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        float scale = displayMetrics.density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * convert px to its equivalent sp
     *
     * Convert px to sp
     */
    public static int px2sp(Context context, float pxValue) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        final float fontScale = displayMetrics.scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * convert sp to its equivalent px
     *
     * Convert sp to px
     */
    public static int sp2px(Context context, float spValue) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        final float fontScale = displayMetrics.scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
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

    /**
     * Obtains the pixel value of the screen height.
     *
     * @param context Context object ApplicationContext
     * @return Height px
     */
    public static int screenHeight(Context context) {
        Resources resources = context.getResources();
        if (resources == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (displayMetrics == null) {
            return 0;
        }
        return displayMetrics.heightPixels;
    }

    /**
     * Screen size
     *
     * @param context context
     * @return Return Value
     */
    public static String screenWandH(Context context) {
        String width = String.valueOf(context.getResources().getDisplayMetrics().widthPixels);
        String height = String.valueOf(context.getResources().getDisplayMetrics().heightPixels);
        return width + "x" + height;
    }

    /**
     * Obtains the pixel density unit of the screen.
     *
     * @param context Context object ApplicationContext
     * @return Return Value
     */
    public static int screenDPI(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }
}
