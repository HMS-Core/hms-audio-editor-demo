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
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.HAEConstant;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.OnTransformCallBack;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.ui.api.AudioInfo;
import com.huawei.hms.audioeditor.ui.common.bean.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Audio Format Conversion
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

    // Whether the format conversion task is in progress
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
        /* *  Obtaining the Storage Path  * */
        pathAudioFormat.setText(
                String.format(
                        Locale.ROOT, getString(R.string.save_path), FileUtil.getAudioFormatStorageDirectory(this)));
    }

    /**
     * Converts the audio format to all files in the list.
     */
    private void convertAllAudio() {
        if (mAudioList != null && !mAudioList.isEmpty()) {
            String filePath = mAudioList.get(0);
            // The sample code passes the input and output audio paths.
            // Only serial tasks are supported. Multiple tasks are not supported.
            int start = filePath.lastIndexOf("/");
            int end = filePath.lastIndexOf(".");
            String name = filePath.substring(start, end);
            String outPutPath = FileUtil.getAudioFormatStorageDirectory(getBaseContext()) + name + "." + transferFormat;

            transformAudio(filePath,outPutPath);

            // Example 2: Only the input and output audio formats (such as MP3) are transferred, and the output files are stored in the default path.
            // Only serial tasks are supported. Multiple tasks are not supported.
            // transformAudioByForm(filePath);
        }
    }

    // Example code 1 (directly uploading the file to the user-defined path)
    private final void transformAudio(String srcFile,String outPutPath) {
        int start = srcFile.lastIndexOf("/");
        int end = srcFile.lastIndexOf(".");
        String name = srcFile.substring(start, end);
        // Transfer the source file path and target file path.
        // Input file path, for example, /sdcard/Music/AudioEdit/audio/music.mp3.
        // Path of the output file (audio format (for example, aac) as the suffix), for example, /sdcard/Music/AudioEdit/format/music.aac
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
                                if (errorCode == HAEErrorCode.FAIL_FILE_EXIST) {
                                    Toast.makeText(
                                            AudioFormatActivity.this,
                                            getResources().getString(R.string.file_exists),
                                            Toast.LENGTH_LONG)
                                            .show();
                                    EditDialogFragment.newInstance(
                                            "",
                                            name,
                                            (newName, dialog) -> {
                                                String outPutPath = FileUtil.getAudioFormatStorageDirectory(getBaseContext()) + newName + "." + transferFormat;
                                                transformAudio(srcFile ,outPutPath);
                                                dialog.dismiss();
                                            })
                                            .show(getSupportFragmentManager(), "EditDialogFragment");
                                } else {
                                    Toast.makeText(AudioFormatActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT)
                                            .show();
                                }
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

    // Example code 2(The converted format is stored in the default path.)
    private final void transformAudioByForm(String srcFile) {
        // srcFile Pathway for interpolation text Like/sdcard/Music/AudioEdit/audio/music.aac
        // transferFormat Format to be converted (for example, mp3). The output file is stored in the default path /sdcard/Music/AudioEdit/format/.
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
        /* *  Return to the button click event.  * */
        backAudioFormat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        /* *  Format List Selection  * */
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

        /* *  Conversion button click event  * */
        transferAudioFormat.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isTansforming) {
                            Toast.makeText(getBaseContext(), "There is a format conversion task in progress.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        /* *  Check whether the format is selected.  * */
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
            // Path transferred in AudioInfo format
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
            // Transfer path as a character string.
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
