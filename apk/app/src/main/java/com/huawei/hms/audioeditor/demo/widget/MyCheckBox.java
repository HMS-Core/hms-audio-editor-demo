/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo.widget;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.huawei.hms.audioeditor.demo.R;

public class MyCheckBox extends LinearLayout {

    private CheckBox checkBox;

    public MyCheckBox(Context context) {
        super(context);
        checkBox = (CheckBox) View.inflate(context, R.layout.my_checkbox, null);
        checkBox.setTextColor(getResources().getColor(R.color.white));
        addView(checkBox);
    }

    public void setText(String text) {
        checkBox.setText(text);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        checkBox.setOnCheckedChangeListener(listener);
    }


}
