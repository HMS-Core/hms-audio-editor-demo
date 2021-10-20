/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.hms.audioeditor.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
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
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.demo.widget.ProgressDialog;
import com.huawei.hms.audioeditor.sdk.AudioExtractCallBack;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
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
    private final int PERMISSION_TYPE_EDIT = 1;

    // 提取
    private final int PERMISSION_TYPE_EXTRACT = 2;

    // 格式转化
    private final int PERMISSION_TYPE_FORMAT = 3;

    // 文件
    private final int PERMISSION_TYPE_FILE = 4;

    // 流式
    private final int PERMISSION_TYPE_FLOW = 5;

    // 文字转语音
    private final int PERMISSION_TYPE_TEXT_TO_AUDIO = 6;

    // 空间渲染
    private final int PERMISSION_TYPE_SPACE = 8;

    // 素材下载
    private final int PERMISSION_MATERIAL = 7;

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
    private LinearLayout mTextToAudio;
    private LinearLayout mMaterial;
    private LinearLayout mLLSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        // 设置SDK的apikey
        HAEApplication.getInstance().setApiKey("CwEAAAAAgcZ0VHghjbgMNepCshMBYMrI2gz95T5FaZxi3uLwCelE1mHjKDMFG12FU10eqCo89KLyreLfXk6winssUXdvwYgh7LQ=");
    }

    // 请求权限
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 先判断有没有权限
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, PERMISSION_REQUESTS);
            } else {
                jumpActivity();
            }
            return;
        }

        PermissionUtils.checkMorePermissions(
                mContext,
                PERMISSIONS,
                new PermissionUtils.PermissionCheckCallBack() {
                    @Override
                    public void onHasPermission() {
                        jumpActivity();
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

    private void jumpActivity() {
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
        } else if (currentPermissionType == PERMISSION_TYPE_TEXT_TO_AUDIO) {
            startAiDubbingActivity();
        } else if (currentPermissionType == PERMISSION_MATERIAL) {
            startMaterialActivity();
        } else if (currentPermissionType == PERMISSION_TYPE_SPACE){
            startSpaceActivity();
        }
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
        mTextToAudio.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_TEXT_TO_AUDIO;
                    requestPermission();
                });
        mLLSpace.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_TYPE_SPACE;
                    requestPermission();
                });

        mMaterial.setOnClickListener(
                v -> {
                    currentPermissionType = PERMISSION_MATERIAL;
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
        mTextToAudio = findViewById(R.id.ll_text_to_audio);
        mMaterial = findViewById(R.id.ll_material);
        mLLSpace = findViewById(R.id.ll_text_to_space);
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

    // 启动文字转语音页面
    private void startAiDubbingActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, AiDubbingAudioActivity.class);
        startActivity(safeIntent);
    }

    // 启动素材下载页面
    private void startMaterialActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, MaterialsActivity.class);
        startActivity(safeIntent);
    }

    // 启动空间渲染
    private void startSpaceActivity() {
        Intent safeIntent = new Intent(new Intent());
        safeIntent.setClass(this, SpaceRenderActivity.class);
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
                            } else if (currentPermissionType == PERMISSION_TYPE_TEXT_TO_AUDIO) {
                                startAiDubbingActivity();
                            }else if (currentPermissionType == PERMISSION_TYPE_SPACE){
                                startSpaceActivity();
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
        String name = "audio_extract";
        if (path != null) {
            int slashIndex = path.lastIndexOf("/");
            if (slashIndex == -1) {
                name = path;
            } else {
                name = path.substring(slashIndex + 1);
            }
            int dotIndex = name.lastIndexOf(".");
            if (dotIndex >= 0) {
                name = name.substring(0, dotIndex);
            }
        }
        realExtractAudio(path, outPutDir, name);
    }

    private void realExtractAudio(String path, String outPutDir, String name) {
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
                                        () -> {
                                            fragmentDialog.dismiss();
                                            String format = getResources().getString(R.string.extract_success);
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    String.format(format, audioPath),
                                                    Toast.LENGTH_LONG)
                                                    .show();
                                        });
                            }

                            @Override
                            public void onProgress(int progress) {
                                Log.d(TAG, "ExtractAudio onProgress : " + progress);
                                runOnUiThread(() -> fragmentDialog.setProgress(progress));
                            }

                            @Override
                            public void onFail(int errCode) {
                                Log.i(TAG, "ExtractAudio onFail : " + errCode);
                                runOnUiThread(
                                        () -> {
                                            fragmentDialog.dismiss();
                                            if (errCode == HAEErrorCode.FAIL_FILE_EXIST) {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        getResources().getString(R.string.file_exists),
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                                EditDialogFragment.newInstance(
                                                        "",
                                                        name,
                                                        (newName, dialog) -> {
                                                            realExtractAudio(path, outPutDir, newName);
                                                            dialog.dismiss();
                                                        })
                                                        .show(getSupportFragmentManager(), "EditDialogFragment");
                                            } else {
                                                Toast.makeText(
                                                        MainActivity.this,
                                                        getResources().getString(R.string.extract_fail)
                                                                + " , errCode : "
                                                                + errCode,
                                                        Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        });
                            }

                            @Override
                            public void onCancel() {
                                Log.d(TAG, "ExtractAudio onCancel.");
                                runOnUiThread(
                                        () -> {
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    getResources().getString(R.string.dm_extract_cancel),
                                                    Toast.LENGTH_LONG)
                                                    .show();
                                            fragmentDialog.dismiss();
                                        });
                            }
                        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_REQUESTS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                jumpActivity();
            } else {
                Toast.makeText(this, getResources().getString(R.string.no_write_permission), Toast.LENGTH_SHORT).show();
            }
        }

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
