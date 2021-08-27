/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.audioeditor.common.agc.HAEApplication;
import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.demo.util.PermissionUtils;
import com.huawei.hms.audioeditor.demo.widget.ProgressDialog;
import com.huawei.hms.audioeditor.sdk.AudioExtractCallBack;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.ui.api.AudioExportCallBack;
import com.huawei.hms.audioeditor.ui.api.AudioInfo;
import com.huawei.hms.audioeditor.ui.api.HAEUIManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // 权限请求码
    private static final int PERMISSION_REQUESTS = 1;

    // 选择视频的请求码
    private static final int REQUEST_CODE_FOR_SELECT_VIDEO = 1000;

    // 编辑
    private static final int PERMISSION_TYPE_EDIT = 1;

    // 提取
    private static final int PERMISSION_TYPE_EXTRACT = 2;

    // 格式转化
    private static final int PERMISSION_TYPE_FORMAT = 3;

    // 文件
    private static final int PERMISSION_TYPE_FILE = 4;

    // 流式
    private static final int PERMISSION_TYPE_FLOW = 5;

    // 当前权限请求类型
    private int currentPermissionType = PERMISSION_TYPE_EDIT;
    public ProgressDialog fragmentDialog;
    private LinearLayout startEdit;
    private LinearLayout extractAudio;
    private LinearLayout changeSound;
    private LinearLayout formatMain;
    private LinearLayout eqMain;
    private ImageView mSetting;
    private Context mContext;
    private final String[] PERMISSIONS =
            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        // 设置SDK的apikey
        HAEApplication.getInstance()
                .setApiKey(
                        "CwEAAAAAXFybAXyoaI1/2+iQSHBdQU0CW8oCSbD2jWTr6pfvtGUwoRhZUQRXO/3Y2RqkjG30ch0uIAZrmWkpVpItE6ZewOqJcmA=");
    }

    // 请求权限
    private void requestPermission() {
        PermissionUtils.checkMorePermissions(
                mContext,
                PERMISSIONS,
                new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {
                        if (currentPermissionType == PERMISSION_TYPE_EDIT) {
                            startUIActivity();
                        } else if (currentPermissionType == PERMISSION_TYPE_EXTRACT) {
                            extractAudio();
                        } else if (currentPermissionType == PERMISSION_TYPE_FORMAT) {
                            startFormatActivity();
                        } else if (currentPermissionType == PERMISSION_TYPE_FILE) {
                            startFileApiActivity();
                        } else if (currentPermissionType == PERMISSION_TYPE_FLOW) {
                            startFlowApiActivity();
                        }
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDown(String... permission) {
                        PermissionUtils.requestMorePermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS);
                    }

                    @Override
                    public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                        PermissionUtils.requestMorePermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initEvent() {
        startEdit.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_EDIT;
                    requestPermission();
                });
        formatMain.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_FORMAT;
                    requestPermission();
                });
        eqMain.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_FILE;
                    requestPermission();
                });

        mSetting.setOnClickListener(
                v -> {
                    this.startActivity(new Intent(MainActivity.this, SettingActivity.class));
                });

        extractAudio.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_EXTRACT;
                    requestPermission();
                });

        changeSound.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_FLOW;
                    requestPermission();
                });
    }

    private void initView() {
        startEdit = findViewById(R.id.start_edit);
        formatMain = findViewById(R.id.format_main);
        changeSound = findViewById(R.id.change_sound);
        eqMain = findViewById(R.id.eq_main);
        mSetting = findViewById(R.id.setting);
        extractAudio = findViewById(R.id.extract_main);
    }

    // The default UI is displayed.

    /**
     * 导入音频，进入音频编辑主界面管理类
     */
    private void startUIActivity() {
        HAEUIManager.getInstance().launchEditorActivity(this);
    }

    // 启动音频格式转换页面
    private void startFormatActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, AudioFormatActivity.class);
        startActivity(safeIntent);
    }

    // 启动文件接口页面
    private void startFileApiActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, FileApiActivity.class);
        startActivity(safeIntent);
    }

    // 启动流式接口页面
    private void startFlowApiActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, StreamApiActivity.class);
        startActivity(safeIntent);
    }

    // Export interface callback
    private static AudioExportCallBack callBack =
            new AudioExportCallBack() {
                @Override
                public void onAudioExportSuccess(AudioInfo audioInfo) {
                    String mediaPath = audioInfo.getAudioPath();
                    Log.i("MainActivity", "The current audio export path is" + mediaPath);
                }

                @Override
                public void onAudioExportFailed(int i) {}
            };

    /**
     * Display Go to App Settings Dialog
     */
    private void showToAppSettingDialog() {
        new AlertDialog.Builder(this)
                .setMessage("")
                .setPositiveButton(
                        getString(R.string.setting), (dialog, which) -> PermissionUtils.toAppSetting(mContext))
                .setNegativeButton(getString(R.string.cancels), null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUESTS) {
            PermissionUtils.onRequestMorePermissionsResult(
                    mContext,
                    PERMISSIONS,
                    new PermissionUtils.PermissionCheckCallBack() {
                        @Override
                        public void onHasPermission() {
                            if (currentPermissionType == PERMISSION_TYPE_EDIT) {
                                startUIActivity();
                            } else if (currentPermissionType == PERMISSION_TYPE_EXTRACT) {
                                extractAudio();
                            } else if (currentPermissionType == PERMISSION_TYPE_FILE) {
                                startFileApiActivity();
                            } else if (currentPermissionType == PERMISSION_TYPE_FLOW) {
                                startFlowApiActivity();
                            }
                        }

                        @Override
                        public void onUserHasAlreadyTurnedDown(String... permission) {}

                        @Override
                        public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {
                            showToAppSettingDialog();
                        }
                    });
        }
    }

    /**
     * 提取音频操作
     */
    private void extractAudio() {
        // 获取视频文件
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.setType("video/*");
        // 取得视频后返回本画面
        startActivityForResult(intent, REQUEST_CODE_FOR_SELECT_VIDEO);
    }

    /**
     * 通过视频文件路径提取视频中的音频文件
     *
     * @param path 视频文件完整路径
     */
    private void beginExtractAudio(String path) {
        fragmentDialog = ProgressDialog.newInstance("提取中");
        fragmentDialog.show(getSupportFragmentManager(), "ProgressDialogFragment");
        String outPutDir = FileUtil.getAudioExtractStorageDirectory(this);
        String name = "audio_extract_" + System.currentTimeMillis();
        if (path != null) {
            int slashIndex = path.lastIndexOf("/");
            if (slashIndex == -1) {
                name = path;
            } else {
                name = path.substring(slashIndex + 1);
            }
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex >= 0) {
                name = name.substring(0, dotIndex) + "_" + System.currentTimeMillis();
            }
        }
        // 开始提取音频
        HAEAudioExpansion.getInstance()
                .extractAudio(
                        this,
                        path,
                        outPutDir,
                        name,
                        new AudioExtractCallBack() {
                            @Override
                            public void onSuccess(String audioPath) {
                                Log.d(TAG, "ExtractAudio onSuccess : " + audioPath);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                fragmentDialog.dismiss();
                                                String format = getResources().getString(R.string.extract_success);
                                                Toast.makeText(
                                                                MainActivity.this,
                                                                String.format(format, audioPath),
                                                                Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        });
                            }

                            @Override
                            public void onProgress(int progress) {
                                Log.d(TAG, "ExtractAudio onProgress : " + progress);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                fragmentDialog.setProgress(progress);
                                            }
                                        });
                            }

                            @Override
                            public void onFail(int errCode) {
                                Log.i(TAG, "ExtractAudio onFail : " + errCode);
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                                MainActivity.this,
                                                                getResources().getString(R.string.extract_fail)
                                                                        + " , errCode : "
                                                                        + errCode,
                                                                Toast.LENGTH_LONG)
                                                        .show();
                                                fragmentDialog.dismiss();
                                            }
                                        });
                            }

                            @Override
                            public void onCancel() {
                                Log.d(TAG, "ExtractAudio onCancel.");
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(
                                                                MainActivity.this,
                                                                getResources().getString(R.string.dm_extract_cancel),
                                                                Toast.LENGTH_LONG)
                                                        .show();
                                                fragmentDialog.dismiss();
                                            }
                                        });
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_SELECT_VIDEO) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getResources().getString(R.string.select_none_video), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    String filePath = FileUtils.getRealPath(this, uri);
                    Log.i(TAG, filePath);
                    if (!TextUtils.isEmpty(filePath)) {
                        beginExtractAudio(filePath);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.file_not_avable), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }
}
