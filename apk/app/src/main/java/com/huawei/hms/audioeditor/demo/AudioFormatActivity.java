/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.demo.util.SampleConstant;
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion;
import com.huawei.hms.audioeditor.sdk.HAEConstant;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.OnTransformCallBack;
import com.huawei.hms.audioeditor.sdk.bean.HAEAudioFormat;
import com.huawei.hms.audioeditor.sdk.bean.HAEAudioTransformConfig;
import com.huawei.hms.audioeditor.sdk.util.FileUtil;
import com.huawei.hms.audioeditor.sdk.util.SmartLog;
import com.huawei.hms.audioeditor.ui.api.AudioInfo;
import com.huawei.hms.audioeditor.ui.common.bean.Constant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Audio Format Conversion
 */
public class AudioFormatActivity extends AppCompatActivity {
    private static final String TAG = "AudioFormatActivity";

    private static final int SELECT_AUDIOS_REQUEST_CODE = 1000;

    private static final String AUDIO_PATH = "audioPath";
    private static final String AUDIO_NAME = "audioName";

    private ImageView backAudioFormat;
    private TextView pathAudioFormat;
    private RadioGroup radioGroupAudioFormat;
    private TextView transferAudioFormat;
    private TextView transferAudioBaseFormat;
    private TextView audioName;
    private ProgressBar progressBar;
    private String transferFormat = "";

    private EditText editTextSampleRate;
    private EditText editTextChannel;
    private EditText editTextBitRate;
    private EditText editTextFormat;

    // Whether the format conversion task is in progress
    private boolean isTansforming = false;

    private List<String> mAudioList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SmartLog.d(TAG, "onCreate");
        setContentView(R.layout.activity_audio_format);
        initView();
        initData(savedInstanceState);
        initEvent();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAudioList != null && mAudioList.size() > 0) {
            outState.putString(AUDIO_PATH, mAudioList.get(0));
            outState.putString(AUDIO_NAME, audioName.getText().toString());
        }
    }

    private void initView() {
        backAudioFormat = findViewById(R.id.back_fragment_audio_format);
        pathAudioFormat = findViewById(R.id.path_fragment_audio_format);
        radioGroupAudioFormat = findViewById(R.id.radio_group_fragment_audio_format);
        transferAudioFormat = findViewById(R.id.transfer_fragment_audio_format);
        transferAudioBaseFormat = findViewById(R.id.transfer_fragment_audio_base_format);
        progressBar = findViewById(R.id.progress_recycler_view_layout_audio_format_item);
        audioName = findViewById(R.id.audio_name);
        editTextSampleRate = findViewById(R.id.transfer_fragment_audio_samplaterate);
        editTextChannel = findViewById(R.id.transfer_fragment_audio_channel);
        editTextBitRate = findViewById(R.id.transfer_fragment_audio_bitrate);
        editTextFormat = findViewById(R.id.transfer_fragment_audio_formatedit);
    }

    private void initData(Bundle savedInstanceState) {
        mAudioList = new ArrayList<>();
        if (savedInstanceState != null) {
            String oldAudioPath = savedInstanceState.getString(AUDIO_PATH);
            String oldAudioName = savedInstanceState.getString(AUDIO_NAME);
            if (!TextUtils.isEmpty(oldAudioPath)) {
                mAudioList.add(oldAudioPath);
            }
            audioName.setText(TextUtils.isEmpty(oldAudioName) ? "" : oldAudioName);
        } else {
            try {
                Intent intent = new Intent(SampleConstant.CHOOSE_AUDIO_ACTION);
                startActivityForResult(intent, SELECT_AUDIOS_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // "can't find the choose audio activity"
                SmartLog.e(TAG, e.getMessage());
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

            transformAudio(filePath, outPutPath);

            // Example 2: Only the input and output audio formats (such as MP3) are transferred, and the output files are stored in the default path.
            // Only serial tasks are supported. Multiple tasks are not supported.
            // transformAudioByForm(filePath);
        }
    }

    // Example code 1 (directly uploading the file to the user-defined path)
    private final void transformAudio(String srcFile, String outPutPath) {
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
                    public void onFail(int errorCode, String errorMsg) {
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
                                    transformAudio(srcFile, outPutPath);
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
                    editTextFormat.setText(SampleConstant.AUDIO_TYPE_MP3);
                } else if (checkedId == R.id.radio_button_2_fragment_audio_format) {
                    transferFormat = SampleConstant.AUDIO_TYPE_WAV;
                    editTextFormat.setText(SampleConstant.AUDIO_TYPE_WAV);
                } else if (checkedId == R.id.radio_button_3_fragment_audio_format) {
                    transferFormat = SampleConstant.AUDIO_TYPE_FLAC;
                    editTextFormat.setText(SampleConstant.AUDIO_TYPE_FLAC);
                }
            });

        /* *  Conversion button click event  * */
        transferAudioBaseFormat.setOnClickListener(v -> {
            if (isTansforming) {
                Toast.makeText(getBaseContext(), "There is a format conversion task in progress.", Toast.LENGTH_SHORT).show();
                return;
            }
            /* *  Check whether the format is selected.  * */
            convertBaseAudioFormat();
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
                // mAudioList: Indicates the path that is entered from an external system. Validity verification is required.
                mAudioList = (ArrayList<String>) data.getSerializableExtra(HAEConstant.AUDIO_PATH_LIST);
                if (mAudioList != null && !mAudioList.isEmpty()) {
                    File file = new File(mAudioList.get(0));
                    audioName.setText(file.getName());
                }
            }
            if (mAudioList != null && mAudioList.size() > 0) {
                getAndShowFormat(mAudioList.get(0));
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

    private void getAndShowFormat(String audioPath) {
        HAEAudioFormat audioInfos = HAEAudioExpansion.getInstance().getAudioFormat(audioPath);
        if (audioInfos == null || !audioInfos.isValidAudio()) {
            SmartLog.e(TAG, "the path:" + audioPath + " is not support");
            editTextBitRate.setText("-1");
            editTextBitRate.setEnabled(false);
            editTextSampleRate.setText("-1");
            editTextSampleRate.setEnabled(false);
            editTextChannel.setText("-1");
            editTextChannel.setEnabled(false);
            editTextFormat.setText("");
            editTextFormat.setEnabled(false);
            return;
        }
        editTextFormat.setText(audioInfos.getFormat());
        editTextFormat.setEnabled(true);
        editTextBitRate.setText(String.valueOf(audioInfos.getBitRate()));
        editTextBitRate.setEnabled(true);
        editTextSampleRate.setText(String.valueOf(audioInfos.getSampleRate()));
        editTextSampleRate.setEnabled(true);
        editTextChannel.setText(String.valueOf(audioInfos.getChannels()));
        editTextChannel.setEnabled(true);
    }

    private void transAudioFormat(String inFilePath, String outPutPath, HAEAudioTransformConfig config) {
        int start = inFilePath.lastIndexOf("/");
        int end = inFilePath.lastIndexOf(".");
        String name = inFilePath.substring(start, end);
        HAEAudioExpansion.getInstance().transformAudio(inFilePath, outPutPath, config, new OnTransformCallBack() {
            @Override
            public void onProgress(int progress) {
                isTansforming = true;
                progressBar.setProgress(progress);
                Log.i(TAG, "transformAudioFormat onProgress:" + progress);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                isTansforming = false;
                Log.e(TAG, "transformAudioFormat onFail, code:" + errorCode + " msg:" + msg);
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
                                int end = outPutPath.lastIndexOf(".");
                                String format = outPutPath.substring(end);
                                String outPutPath = FileUtil.getAudioFormatStorageDirectory(getBaseContext()) + newName + "_" +
                                    config.getSampleRate() + "_" +
                                    config.getBitRate() + "_" +
                                    config.getChannels() + "_" + format;
                                transAudioFormat(inFilePath, outPutPath, config);
                                dialog.dismiss();
                            })
                        .show(getSupportFragmentManager(), "EditDialogFragment");
                }
                runOnUiThread(() ->Toast.makeText(getBaseContext(), "ErrorCode : " + errorCode + " ErrorMsg：" + msg, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onSuccess(String path) {
                isTansforming = false;
                Log.i(TAG, "transformAudioFormat onSuccess, path:" + path);
                runOnUiThread(() -> Toast.makeText(getBaseContext(), "Success: " + path, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancel() {
                isTansforming = false;
                Log.w(TAG, "transformAudioFormat onCancel");
                runOnUiThread(() -> Toast.makeText(getBaseContext(), "Cancel", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void convertBaseAudioFormat() {
        if (mAudioList == null || mAudioList.size() == 0) {
            Toast.makeText(getBaseContext(),
                getString(R.string.tranfer_error_nopath),
                Toast.LENGTH_SHORT).show();
            return;
        }
        int sampleRate = 0;
        int bitRate = 0;
        int channel = 0;
        if (!TextUtils.isEmpty(editTextSampleRate.getText())) {
            sampleRate = Integer.parseInt(editTextSampleRate.getText().toString());
        }
        if (!TextUtils.isEmpty(editTextBitRate.getText())) {
            bitRate = Integer.parseInt(editTextBitRate.getText().toString());
        }
        if (!TextUtils.isEmpty(editTextChannel.getText())) {
            channel = Integer.parseInt(editTextChannel.getText().toString());
        }
        HAEAudioTransformConfig config = new HAEAudioTransformConfig();
        config.setBitRate(bitRate);
        config.setChannels(channel);
        config.setSampleRate(sampleRate);
        String filePath = mAudioList.get(0);
        int start = filePath.lastIndexOf("/");
        int end = filePath.lastIndexOf(".");
        String name = filePath.substring(start, end);
        String format = editTextFormat.getText().toString();
        String outPutPath = FileUtil.getAudioFormatStorageDirectory(getBaseContext()) + name + "_" +
            sampleRate + "_" +
            bitRate + "_" +
            channel + "_." + format;
        transAudioFormat(filePath, outPutPath, config);
    }
}
