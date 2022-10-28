/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import java.math.BigDecimal;

/**
 * tools
 *
 * @since 20200202
 */
public class BigDecimalUtils {
    /*
     * The exact number of decimal places.
     */
    private static final int DEF_DIV_SCALE = 10;

    /**
     * Provides (relatively) precise division operations, accurate to 10 decimal places, rounded to the nearest 10 digits in the event of inexhaustible division.
     *
     * @param v1 dividends
     * @param v2 divisor
     * @return quotient of two parameters
     */
    public static float div(float v1, float v2) {
        if (v2 == 0) {
            return v1;
        }
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /**
     * Provides (relatively) accurate division operations.When inexhaustible divisibility occurs, the precision is specified by the scale parameter, and subsequent numbers are rounded off.
     *
     * @param v1    dividends
     * @param v2    divisor
     * @param scale The value must be accurate to several decimal places.。
     * @return quotient of two parameters
     */
    public static float div(float v1, float v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

}
