/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.audioeditor.demo.util.SampleConstant;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.HAEConstant;
import com.huawei.hms.audioeditor.sdk.OnTransformCallBack;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.ui.api.AudioInfo;
import com.huawei.hms.audioeditor.ui.common.bean.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 音频格式转换
 */
public class AudioFormatActivity extends AppCompatActivity {
    private static final String TAG = "AudioFormatActivity";

    private final int SELECT_AUDIOS_REQUEST_CODE = 1000;

    private final String AUDIO_PATH = "audioPath";
    private final String AUDIO_NAME = "audioName";

    private ImageView backAudioFormat;
    private TextView pathAudioFormat;
    private RadioGroup radioGroupAudioFormat;
    private TextView transferAudioFormat;
    private TextView audioName;
    private ProgressBar progressBar;
    private String transferFormat = "";

    // 格式转换任务是否进行中
    private boolean isTansforming = false;

    private List<String> mAudioList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_audio_format);
        initView();
        initData(savedInstanceState);
        initEvent();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");
        if(mAudioList!=null && mAudioList.size()>0){
            outState.putString(AUDIO_PATH,mAudioList.get(0));
            outState.putString(AUDIO_NAME,audioName.getText().toString());
        }
    }

    private void initView() {
        backAudioFormat = findViewById(R.id.back_fragment_audio_format);
        pathAudioFormat = findViewById(R.id.path_fragment_audio_format);
        radioGroupAudioFormat = findViewById(R.id.radio_group_fragment_audio_format);
        transferAudioFormat = findViewById(R.id.transfer_fragment_audio_format);
        progressBar = findViewById(R.id.progress_recycler_view_layout_audio_format_item);
        audioName = findViewById(R.id.audio_name);
    }

    private void initData(Bundle savedInstanceState) {
        mAudioList = new ArrayList<>();
        if(savedInstanceState != null){
            String oldAudioPath = savedInstanceState.getString(AUDIO_PATH);
            String oldAudioName = savedInstanceState.getString(AUDIO_NAME);
            if(!TextUtils.isEmpty(oldAudioPath)){
                mAudioList.add(oldAudioPath);
            }
            audioName.setText(TextUtils.isEmpty(oldAudioName)?"":oldAudioName);
        }else{
            try {
                Intent intent = new Intent(SampleConstant.CHOOSE_AUDIO_ACTION);
                startActivityForResult(intent, SELECT_AUDIOS_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // "can't find the choose audio activity"
            }
        }
        /* *  获取存储路径  * */
        pathAudioFormat.setText(
                String.format(
                        Locale.ROOT, getString(R.string.save_path), FileUtil.getAudioFormatStorageDirectory(this)));
    }

    /**
     * 音频格式转换 转换所有的list里的文件
     */
    private void convertAllAudio() {
        if (mAudioList != null && !mAudioList.isEmpty()) {
            String filePath = mAudioList.get(0);
            // 示例代码一 传输入音频 和 输出音频的路径
            // 只支持任务串行，不支持多任务
            transformAudio(filePath);

            // 示例代码二  只传输入音频和 输出音频格式（如MP3），输出文件到默认路径
            // 只支持任务串行，不支持多任务
            // transformAudioByForm(filePath);
        }
    }

    // 示例代码一（直接传用户自己定义路径文件）
    private final void transformAudio(String srcFile) {
        int start = srcFile.lastIndexOf("/");
        int end = srcFile.lastIndexOf(".");
        String name = srcFile.substring(start, end);
        String outPutPath = FileUtil.getAudioFormatStorageDirectory(getBaseContext()) + name + "." + transferFormat;
        // 传源文件路径 和 目标文件路径
        // 输入文件的路径 如/sdcard/AudioEdit/audio/music.mp3
        // 输出的文件的路径(音频格式（如aac）作为后缀名)，如/sdcard/AudioEdit/format/music.aac
        HAEAudioExpansion.getInstance()
                .transformAudio(
                        getBaseContext(),
                        srcFile,
                        outPutPath,
                        new OnTransformCallBack() {
                            @Override
                            public void onProgress(int progress) {
                                isTansforming = true;
                                progressBar.setProgress(progress);
                            }

                            @Override
                            public void onFail(int errorCode) {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "fail: " + errorCode, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(String outPutPath) {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "Success: " + outPutPath, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancel() {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "Cancel", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    // 示例代码二（用户传转换的格式，存储到默认路径）
    private final void transformAudioByForm(String srcFile) {
        // srcFile 输入文件的路径 如/sdcard/AudioEdit/audio/music.aac
        // transferFormat 需要转换的格式（如mp3），输出文件到默认路径/sdcard/AudioEdit/format/
        HAEAudioExpansion.getInstance()
                .transformAudioUseDefaultPath(
                        getBaseContext(),
                        srcFile,
                        transferFormat,
                        new OnTransformCallBack() {
                            @Override
                            public void onProgress(int progress) {
                                isTansforming = true;
                                progressBar.setProgress(progress);
                            }

                            @Override
                            public void onFail(int errorCode) {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "fail: " + errorCode, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(String outPutPath) {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "Success: " + outPutPath, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancel() {
                                isTansforming = false;
                                Toast.makeText(getBaseContext(), "Cancel", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    protected void initEvent() {
        /* *  返回按钮点击事件  * */
        backAudioFormat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        /* *  格式列表选择  * */
        radioGroupAudioFormat.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    if (checkedId == R.id.radio_button_1_fragment_audio_format) {
                        transferFormat = SampleConstant.AUDIO_TYPE_MP3;
                    } else if (checkedId == R.id.radio_button_2_fragment_audio_format) {
                        transferFormat = SampleConstant.AUDIO_TYPE_WAV;
                    } else if (checkedId == R.id.radio_button_3_fragment_audio_format) {
                        transferFormat = SampleConstant.AUDIO_TYPE_FLAC;
                    }
                });

        /* *  转换按钮点击事件  * */
        transferAudioFormat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTansforming) {
                            Toast.makeText(getBaseContext(), "当前有正在进行的格式转换任务", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        /* *  判断格式是否选择  * */
                        if (transferFormat.isEmpty()) {
                            Toast.makeText(
                                            getBaseContext(),
                                            getString(R.string.audio_format_transfer_toast),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            convertAllAudio();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            finish();
            return;
        }
        if (resultCode == SampleConstant.RESULT_CODE && requestCode == SELECT_AUDIOS_REQUEST_CODE && data != null) {
            // 以AudioInfo形式传过来路径
            if (data.hasExtra(Constant.EXTRA_SELECT_RESULT)) {
                ArrayList<AudioInfo> list =
                        (ArrayList<AudioInfo>) data.getSerializableExtra(SampleConstant.EXTRA_SELECT_RESULT);
                if (list != null && !list.isEmpty()) {
                    for (AudioInfo audioInfo : list) {
                        mAudioList.add(audioInfo.getAudioPath());
                    }
                    audioName.setText(list.get(0).getAudioName());
                }
            }
            // 以字符串形式传过来路径
            if (data.hasExtra(HAEConstant.AUDIO_PATH_LIST)) {
                mAudioList = (ArrayList<String>) data.getSerializableExtra(HAEConstant.AUDIO_PATH_LIST);
                if (mAudioList != null && !mAudioList.isEmpty()) {
                    File file = new File(mAudioList.get(0));
                    audioName.setText(file.getName());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTansforming) {
            HAEAudioExpansion.getInstance().cancelTransformAudio();
        }
    }
}
