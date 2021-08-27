/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    public static final String DEFAULT_VERSION = "1.0.0.300";
    private TextView mBack;
    private TextView mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        initEvent();
    }

    private void initEvent() {
        mBack.setOnClickListener(v -> finish());
    }

    private void initView() {
        mBack = findViewById(R.id.back);
        mVersion = findViewById(R.id.version);
        mVersion.setText(getVersionName());
    }

    /**
     * get App versionName
     *
     * @return version name
     */
    public String getVersionName() {
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo =
                    packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SettingActivity", "Failed to get package version: " + e.getMessage());
        }
        return DEFAULT_VERSION;
    }
}
