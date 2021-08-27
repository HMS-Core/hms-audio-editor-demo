/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.audioeditor.sdk.AudioParameters;
import com.huawei.hms.audioeditor.sdk.HAEChangeVoiceStream;
import com.huawei.hms.audioeditor.sdk.HAEEqualizerStream;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.HAENoiseReductionStream;
import com.huawei.hms.audioeditor.sdk.HAESceneStream;
import com.huawei.hms.audioeditor.sdk.HAESoundFieldStream;
import com.huawei.hms.audioeditor.sdk.SoundType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 演示将采样率为44100，位深为16，声道数为2的pcm数据进行变声、降噪等处理
 * @since 2021/7/15
 */
public class StreamApiActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "StreamApiActivity";
    private ImageView back;
    private EditText etPcmFilePath;
    private RadioGroup rgSoundType;
    private Button beginChange;
    private Button beginPlay;
    private Button beginReduction;
    private RadioGroup rgEnvType;
    private Button beginEvn;
    private RadioGroup rgSoundGround;
    private Button beginSoundGround;
    private RadioGroup rgEq;
    private Button beginEq;
    private AudioTrack mAudioTrack;
    private volatile boolean isPlaying;

    private static final int TYPE_NONE = 0;
    private static final int TYPE_CHANGE_SOUND = 1;
    private static final int TYPE_REDUCTION = 2;
    private static final int TYPE_ENV = 3;
    private static final int TYPE_SOUND_FILED = 4;
    private static final int TYPE_EQ = 5;
    private int currentType = TYPE_NONE;

    @SuppressLint("SdCardPath")
    private String pcmFilePath = "/sdcard/changeSound.pcm";

    private volatile boolean unFinish = true;
    private static final int BIT_DEPTH = 16;
    private static final int CHANNEL_COUNT = 2;
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 7056;

    // 是否保存到文件
    private boolean saveToFile = true;
    private String saveToFilePath = "/sdcard/changeSound-saved";
    private FileOutputStream saveToFileStream = null;

    private HAEChangeVoiceStream haeChangeVoiceStream;
    private HAENoiseReductionStream haeNoiseReductionStream;
    private HAESceneStream haeSceneStream;
    private HAESoundFieldStream haeSoundFieldStream;
    private HAEEqualizerStream haeEqualizerStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_api_show);
        back = findViewById(R.id.back);
        back.setOnClickListener(this);
        etPcmFilePath = findViewById(R.id.pcm_file_path);
        etPcmFilePath.setText(pcmFilePath);
        beginPlay = findViewById(R.id.begin_play);
        beginPlay.setOnClickListener(this);
        beginChange = findViewById(R.id.begin_change);
        beginChange.setOnClickListener(this);
        beginReduction = findViewById(R.id.begin_reduction);
        beginReduction.setOnClickListener(this);
        rgSoundType = findViewById(R.id.rg_sound_type);
        rgSoundType.setOnCheckedChangeListener(this);
        rgEnvType = findViewById(R.id.rg_env_type);
        rgEnvType.setOnCheckedChangeListener(this);
        beginEvn = findViewById(R.id.begin_env);
        beginEvn.setOnClickListener(this);
        rgSoundGround = findViewById(R.id.rg_sound_ground_type);
        rgSoundGround.setOnCheckedChangeListener(this);
        beginSoundGround = findViewById(R.id.begin_sound_ground);
        beginSoundGround.setOnClickListener(this);
        rgEq = findViewById(R.id.rg_eq_type);
        rgEq.setOnCheckedChangeListener(this);
        beginEq = findViewById(R.id.begin_eq);
        beginEq.setOnClickListener(this);

        haeChangeVoiceStream = new HAEChangeVoiceStream();
        haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_SEASONED);

        haeNoiseReductionStream = new HAENoiseReductionStream();

        haeSceneStream = new HAESceneStream();
        haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);

        haeSoundFieldStream = new HAESoundFieldStream();
        haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_WIDE);

        haeEqualizerStream = new HAEEqualizerStream();
        haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_POP_VALUE);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int res = HAEErrorCode.SUCCESS;
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.begin_change:
                res = haeChangeVoiceStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
                if (res != HAEErrorCode.SUCCESS) {
                    Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
                    return;
                }
                beginDealPcmFile(TYPE_CHANGE_SOUND);
                break;
            case R.id.begin_reduction:
                res = haeNoiseReductionStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
                if (res != HAEErrorCode.SUCCESS) {
                    Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
                    return;
                }
                beginDealPcmFile(TYPE_REDUCTION);
                break;
            case R.id.begin_env:
                res = haeSceneStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
                if (res != HAEErrorCode.SUCCESS) {
                    Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
                    return;
                }
                beginDealPcmFile(TYPE_ENV);
                break;
            case R.id.begin_sound_ground:
                res = haeSoundFieldStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
                if (res != HAEErrorCode.SUCCESS) {
                    Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
                    return;
                }
                beginDealPcmFile(TYPE_SOUND_FILED);
                break;
            case R.id.begin_eq:
                res = haeEqualizerStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
                if (res != HAEErrorCode.SUCCESS) {
                    Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
                    return;
                }
                beginDealPcmFile(TYPE_EQ);
                break;
            case R.id.begin_play:
                beginDealPcmFile(TYPE_NONE);
                break;
            default:
                break;
        }
    }

    private void beginDealPcmFile(int type) {
        if (isPlaying) {
            return;
        }
        if (etPcmFilePath != null) {
            pcmFilePath = etPcmFilePath.getText().toString();
        }
        File pcmFile = new File(pcmFilePath);
        if (!pcmFile.exists()) {
            Toast.makeText(this, getResources().getString(R.string.pcm_file_not_exists), Toast.LENGTH_SHORT).show();
            return;
        }
        currentType = type;
        new Thread(
                        () -> {
                            isPlaying = true;
                            FileInputStream fileInputStream = null;
                            try {
                                fileInputStream = new FileInputStream(pcmFile);
                                byte[] buffer = new byte[BUFFER_SIZE];
                                byte[] resultByte = null;
                                if (saveToFile) {
                                    saveToFileStream =
                                            new FileOutputStream(
                                                    new File(
                                                            saveToFilePath
                                                                    + "_"
                                                                    + System.currentTimeMillis()
                                                                    + ".pcm"));
                                }
                                while (fileInputStream.read(buffer) != -1 && unFinish) {
                                    if (currentType == TYPE_CHANGE_SOUND) {
                                        resultByte = haeChangeVoiceStream.applyPcmData(buffer);
                                        playPcm(resultByte);
                                    } else if (currentType == TYPE_REDUCTION) {
                                        resultByte = haeNoiseReductionStream.applyPcmData(buffer);
                                        playPcm(resultByte);
                                    } else if (currentType == TYPE_ENV) {
                                        resultByte = haeSceneStream.applyPcmData(buffer);
                                        playPcm(resultByte);
                                    } else if (currentType == TYPE_SOUND_FILED) {
                                        resultByte = haeSoundFieldStream.applyPcmData(buffer);
                                        playPcm(resultByte);
                                    } else if (currentType == TYPE_EQ) {
                                        resultByte = haeEqualizerStream.applyPcmData(buffer);
                                        playPcm(resultByte);
                                    } else {
                                        playPcm(buffer);
                                    }

                                    // 保存到文件
                                    if (resultByte != null) {
                                        saveToFileStream.write(resultByte);
                                    }
                                }
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            } finally {
                                isPlaying = false;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                }

                                if (saveToFileStream != null) {
                                    try {
                                        saveToFileStream.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, e.getMessage());
                                    }
                                    saveToFileStream = null;
                                }
                            }
                        })
                .start();
    }

    private void playPcm(byte[] pcmData) {
        if (mAudioTrack == null) {
            mAudioTrack =
                    new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            SAMPLE_RATE,
                            AudioFormat.CHANNEL_IN_STEREO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            BUFFER_SIZE,
                            AudioTrack.MODE_STREAM);

            mAudioTrack.play();
        }
        if (pcmData != null && pcmData.length > 0 && mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.write(pcmData, 0, pcmData.length);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_uncle:
                haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_SEASONED);
                break;
            case R.id.rb_lori:
                haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_CUTE);
                break;
            case R.id.rb_female:
                haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_FEMALE);
                break;
            case R.id.rb_male:
                haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_MALE);
                break;
            case R.id.rb_monsters:
                haeChangeVoiceStream.changeSoundType(SoundType.AUDIO_TYPE_MONSTER);
                break;
            case R.id.rb_gb:
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);
                break;
            case R.id.rb_tel:
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_EARPIECE);
                break;
            case R.id.rb_sx:
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_UNDERWATER);
                break;
            case R.id.rb_cd:
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_GRAMOPHONE);
                break;
            case R.id.rb_sound_0:
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_WIDE);
                break;
            case R.id.rb_sound_1:
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_FRONT_FACING);
                break;
            case R.id.rb_sound_2:
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_NEAR);
                break;
            case R.id.rb_sound_3:
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_GRAND);
                break;
            case R.id.rb_pops:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_POP_VALUE);
                break;
            case R.id.rb_classic:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_CLASSICAL_VALUE);
                break;
            case R.id.rb_jazz:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_JAZZ_VALUE);
                break;
            case R.id.rb_rock:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_ROCK_VALUE);
                break;
            case R.id.rb_rb:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_RB_VALUE);
                break;
            case R.id.rb_ballads:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_BALLADS_VALUE);
                break;
            case R.id.rb_dance_music:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_DANCE_MUSIC_VALUE);
                break;
            case R.id.rb_chinese_style:
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_CHINESE_STYLE_VALUE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unFinish = false;
        if (mAudioTrack != null) {
            if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                mAudioTrack.stop();
            }
            mAudioTrack.release();
            mAudioTrack = null;
        }
        haeChangeVoiceStream.release();
        haeNoiseReductionStream.release();
        haeSceneStream.release();
        haeSoundFieldStream.release();
        haeEqualizerStream.release();
        super.onDestroy();
    }
}
