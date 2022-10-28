/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

import android.text.TextUtils;

public class PathUtils {
    private static final String TAG = "PathUtils";

    public static String getFileNameWithoutSuffix(String fullName) {
        if (TextUtils.isEmpty(fullName)) {
            return "";
        }
        int index = fullName.lastIndexOf(".");
        if (index > 0) {
            return fullName.substring(0, index);
        }
        return fullName;
    }

    public static String getFileSuffix(String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            int index = fileName.lastIndexOf(".");
            if (index >= 0) {
                return fileName.substring(index);
            }
        }
        return "";
    }
}
