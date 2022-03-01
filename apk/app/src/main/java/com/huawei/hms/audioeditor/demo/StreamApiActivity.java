/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.sdk.AudioParameters;
import com.huawei.hms.audioeditor.sdk.ChangeVoiceOption;
import com.huawei.hms.audioeditor.sdk.HAEChangeVoiceStream;
import com.huawei.hms.audioeditor.sdk.HAEEqualizerStream;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.HAENoiseReductionStream;
import com.huawei.hms.audioeditor.sdk.HAESceneStream;
import com.huawei.hms.audioeditor.sdk.HAESoundFieldStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Demonstrate that PCM data with sampling rate of 44100, bit depth of 16 and channel number of
 * 2 can be processed, such as noise change and noise reduction.
 *
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
    private AudioTrack mChangeVoiceAudioTrack;
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
    private static final int CHANGE_VOICE_BUFFER_SIZE = 7056;

    // Save to File
    private boolean saveToFile = false;
    private String saveToFilePath = "/sdcard/cachePcm/changeSound-saved";
    private FileOutputStream saveToFileStream = null;

    private HAEChangeVoiceStream haeChangeVoiceStream;
    private HAENoiseReductionStream haeNoiseReductionStream;
    private HAESceneStream haeSceneStream;
    private HAESoundFieldStream haeSoundFieldStream;
    private HAEEqualizerStream haeEqualizerStream;

    private ChangeVoiceOption changeVoiceOption;

    private RadioGroup rgSoundSex;
    private RadioGroup rgSoundPart;
    private SeekBar mSbTones;
    private TextView mTvSeekValue1;

    private RadioButton mRbMan;
    private RadioButton mRbWoman;
    private RadioButton mRbFemale;
    private RadioButton mRbMale;

    private float[] malePitch = {0.8f, 2.3f, 1.9f, 1.2f, 0.7f, 1.0f, 1.5f};
    private float[] femalePitch = {0.5f, 1.4f, 1f, 0.6f, 0.4f, 1.0f, 1.1f};

    private int currentVoiceType = ChangeVoiceOption.VoiceType.SEASONED.ordinal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_api_show);
        initView();
        initAllAbility();
        File savePcmPath = new File(saveToFilePath);
        if (!savePcmPath.exists()) {
            savePcmPath.mkdirs();
        }
    }

    private void initView() {
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

        rgSoundSex = findViewById(R.id.rg_sound_sex);
        rgSoundSex.setOnCheckedChangeListener(this);
        rgSoundPart = findViewById(R.id.rg_sound_part);
        rgSoundPart.setOnCheckedChangeListener(this);

        mSbTones = findViewById(R.id.sb_tones);
        // 0.3-3
        mSbTones.setMax(54);

        mTvSeekValue1 = findViewById(R.id.tv_value_1);
        mTvSeekValue1.setText(0.3 + "");
        mSbTones.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float val = (float) (i + 6) / 20;
                mTvSeekValue1.setText(val + "");
                changeVoiceOption.setPitch(val);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mRbMan = findViewById(R.id.rb_man);
        mRbWoman = findViewById(R.id.rb_woman);
        mRbFemale = findViewById(R.id.rb_female);
        mRbMale = findViewById(R.id.rb_male);
    }

    private void initAllAbility() {
        haeChangeVoiceStream = new HAEChangeVoiceStream();
        changeVoiceOption = new ChangeVoiceOption();
        changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.MALE);
        changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.SEASONED);
        haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
        resetpitch();

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
        switch (v.getId()) {
            case R.id.back :
                onBackPressed();
                break;
            case R.id.begin_change :
                changeVoice();
                break;
            case R.id.begin_reduction :
                reduction();
                break;
            case R.id.begin_env :
                changeEnv();
                break;
            case R.id.begin_sound_ground :
                changeSoundGround();
                break;
            case R.id.begin_eq :
                changeEq();
                break;
            case R.id.begin_play :
                beginDealPcmFile(TYPE_NONE);
                break;
            default :
                break;
        }
    }

    private void changeEq() {
        if (isPlaying) {
            return;
        }
        int res = haeEqualizerStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
        } else {
            beginDealPcmFile(TYPE_EQ);
        }
    }

    private void changeSoundGround() {
        if (isPlaying) {
            return;
        }
        int res = haeSoundFieldStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
        } else {
            beginDealPcmFile(TYPE_SOUND_FILED);
        }
    }

    private void changeEnv() {
        if (isPlaying) {
            return;
        }
        int res = haeSceneStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
        } else {
            beginDealPcmFile(TYPE_ENV);
        }
    }

    private void reduction() {
        if (isPlaying) {
            return;
        }
        int res = haeNoiseReductionStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
        } else {
            beginDealPcmFile(TYPE_REDUCTION);
        }
    }

    private void changeVoice() {
        if (isPlaying) {
            return;
        }
        int res = haeChangeVoiceStream.setAudioFormat(BIT_DEPTH, CHANNEL_COUNT, SAMPLE_RATE);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
            return;
        }
        res = haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
        if (res != HAEErrorCode.SUCCESS) {
            Toast.makeText(StreamApiActivity.this, "err code : " + res, Toast.LENGTH_SHORT).show();
        } else {
            beginDealPcmFile(TYPE_CHANGE_SOUND);
        }
    }

    private void beginDealPcmFile(int type) {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        if (etPcmFilePath != null) {
            pcmFilePath = etPcmFilePath.getText().toString();
        }

        currentType = type;
        new Thread(() -> {
            AssetManager.AssetInputStream fileInputStream = null;
            try {
                fileInputStream = (AssetManager.AssetInputStream) getAssets().open("stream.pcm");
                int bufferSize;
                if (currentType == TYPE_CHANGE_SOUND) {
                    bufferSize = CHANGE_VOICE_BUFFER_SIZE;
                } else {
                    bufferSize = BUFFER_SIZE;
                }
                byte[] buffer = new byte[bufferSize];
                byte[] resultByte = null;
                if (saveToFile) {
                    saveToFileStream = new FileOutputStream(
                            new File(saveToFilePath + "_" + System.currentTimeMillis() + ".pcm"));
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

                            // Save to File
                            if (saveToFile && resultByte != null) {
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
        }).start();
    }

    private void playPcm(byte[] pcmData) {
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE, AudioTrack.MODE_STREAM);

            mAudioTrack.play();
        }
        if (mChangeVoiceAudioTrack == null) {
            mChangeVoiceAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, CHANGE_VOICE_BUFFER_SIZE,
                    AudioTrack.MODE_STREAM);

            mChangeVoiceAudioTrack.play();
        }
        if (pcmData != null && pcmData.length > 0) {
            if (currentType == TYPE_CHANGE_SOUND) {
                if (mChangeVoiceAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    mChangeVoiceAudioTrack.write(pcmData, 0, pcmData.length);
                }
            } else {
                if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    mAudioTrack.write(pcmData, 0, pcmData.length);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_uncle :
                currentVoiceType = ChangeVoiceOption.VoiceType.SEASONED.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.SEASONED);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_lori :
                currentVoiceType = ChangeVoiceOption.VoiceType.CUTE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.CUTE);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_female :
                currentVoiceType = ChangeVoiceOption.VoiceType.FEMALE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.FEMALE);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_male :
                currentVoiceType = ChangeVoiceOption.VoiceType.MALE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.MALE);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_monsters :
                currentVoiceType = ChangeVoiceOption.VoiceType.MONSTER.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.MONSTER);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_robots :
                currentVoiceType = ChangeVoiceOption.VoiceType.ROBOTS.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.ROBOTS);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_cartoon :
                currentVoiceType = ChangeVoiceOption.VoiceType.CARTOON.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.CARTOON);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_gb :
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);
                break;
            case R.id.rb_tel :
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_EARPIECE);
                break;
            case R.id.rb_sx :
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_UNDERWATER);
                break;
            case R.id.rb_cd :
                haeSceneStream.setEnvironmentType(AudioParameters.ENVIRONMENT_TYPE_GRAMOPHONE);
                break;
            case R.id.rb_sound_0 :
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_WIDE);
                break;
            case R.id.rb_sound_1 :
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_FRONT_FACING);
                break;
            case R.id.rb_sound_2 :
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_NEAR);
                break;
            case R.id.rb_sound_3 :
                haeSoundFieldStream.setSoundType(AudioParameters.SOUND_FIELD_GRAND);
                break;
            case R.id.rb_pops :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_POP_VALUE);
                break;
            case R.id.rb_classic :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_CLASSICAL_VALUE);
                break;
            case R.id.rb_jazz :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_JAZZ_VALUE);
                break;
            case R.id.rb_rock :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_ROCK_VALUE);
                break;
            case R.id.rb_rb :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_RB_VALUE);
                break;
            case R.id.rb_ballads :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_BALLADS_VALUE);
                break;
            case R.id.rb_dance_music :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_DANCE_MUSIC_VALUE);
                break;
            case R.id.rb_chinese_style :
                haeEqualizerStream.setEqParams(AudioParameters.EQUALIZER_CHINESE_STYLE_VALUE);
                break;
            case R.id.rb_man :
                changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.MALE);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_woman :
                changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.FEMALE);
                haeChangeVoiceStream.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_center :
                changeVoiceOption.setVocalPart(ChangeVoiceOption.VocalPart.MIDDLE);
                break;
            default :
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unFinish = false;
        releaseAudioTrack();
        releaseAllAbility();
        super.onDestroy();
    }

    private void releaseAllAbility() {
        if (haeChangeVoiceStream != null) {
            haeChangeVoiceStream.release();
        }
        if (haeNoiseReductionStream != null) {
            haeNoiseReductionStream.release();
        }
        if (haeSceneStream != null) {
            haeSceneStream.release();
        }
        if (haeSoundFieldStream != null) {
            haeSoundFieldStream.release();
        }
        if (haeEqualizerStream != null) {
            haeEqualizerStream.release();
        }
    }

    private void releaseAudioTrack() {
        if (mAudioTrack != null) {
            if (mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                mAudioTrack.stop();
            }
            mAudioTrack.release();
            mAudioTrack = null;
        }

        if (mChangeVoiceAudioTrack != null) {
            if (mChangeVoiceAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                mChangeVoiceAudioTrack.stop();
            }
            mChangeVoiceAudioTrack.release();
            mChangeVoiceAudioTrack = null;
        }
    }

    private float getDefaultPitch() {
        float[] pitch;
        if (mRbMan.isChecked()) {
            pitch = malePitch;
        } else {
            pitch = femalePitch;
        }
        return pitch[currentVoiceType];
    }

    private void resetpitch() {
        int pitchProgress = pitchToProgress(getDefaultPitch());
        mSbTones.setProgress(pitchProgress);
    }
    private int pitchToProgress(float pitch) {
        return (int) (pitch * 20 - 6);
    }
}
