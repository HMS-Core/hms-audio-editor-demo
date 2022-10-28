/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.util;

/**
 * String tool class
 * @since 2021/4/19
 */
public class StringUtil {

    /**
     * Determines whether a given string is a blank string.
     * A blank string consists of spaces, tabs, carriage returns,
     * and newline characters. If the input string is null or empty,
     * true is returned.
     *
     * @param input Entered content
     * @return boolean Returned Condition
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) {
            return true;
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }
}
