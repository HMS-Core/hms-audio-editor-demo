/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.content.Context;

import com.huawei.hms.audioeditor.ui.R;

import java.util.Formatter;
import java.util.Locale;

public class TimeUtils {
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] S_TIME_ARGS = new Object[2];

    public static String makeTimeString(Context context, long milliSecs) {
        int secs = milli2Secs(milliSecs);
        if (secs < 0) {
            secs = 0;
        }
        String durationFormat = context.getString(R.string.media_duration_format2);
        sFormatBuilder.setLength(0);
        final Object[] timeArgs = S_TIME_ARGS;
        timeArgs[0] = secs / 60; // Number of minutes
        timeArgs[1] = secs % 60; // Remaining seconds based on minutes
        return sFormatter.format(durationFormat, timeArgs).toString();
    }

    /**
     * Convert milliseconds into seconds, rounding down
     *
     * @param milliSecs time
     * @return Returned Seconds
     */
    public static int milli2Secs(long milliSecs) {
        // Round Down
        return (int) Math.floor(BigDecimalUtils.div(milliSecs, 1000f));
    }
}
