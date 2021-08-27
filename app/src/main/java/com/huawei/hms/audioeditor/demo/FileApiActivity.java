/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hms.audioeditor.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.sdk.AudioParameters;
import com.huawei.hms.audioeditor.sdk.AudioSeparationCallBack;
import com.huawei.hms.audioeditor.sdk.ChangeSoundCallback;
import com.huawei.hms.audioeditor.sdk.HAEAudioSeparationFile;
import com.huawei.hms.audioeditor.sdk.HAEChangeVoiceFile;
import com.huawei.hms.audioeditor.sdk.HAEEqualizerFile;
import com.huawei.hms.audioeditor.sdk.HAENoiseReductionFile;
import com.huawei.hms.audioeditor.sdk.HAESceneFile;
import com.huawei.hms.audioeditor.sdk.HAESoundFieldFile;
import com.huawei.hms.audioeditor.sdk.HAESpaceRenderFile;
import com.huawei.hms.audioeditor.sdk.HAETempoPitch;
import com.huawei.hms.audioeditor.sdk.SoundType;
import com.huawei.hms.audioeditor.sdk.materials.network.SeparationCloudCallBack;
import com.huawei.hms.audioeditor.sdk.materials.network.inner.bean.SeparationBean;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件接口
 *
 * @since 2021/7/30
 */
public class FileApiActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "FileApiActivity";

    private ImageView fileBack;
    private TextView tvFilePath;
    private Button choiceFile;
    private Button cancel;
    private RadioGroup rgFileSoundType;
    private Button beginFileChange;
    private RadioGroup rgFileEnvType;
    private Button beginFileEvn;
    private RadioGroup rgFileSoundGround;
    private Button beginFileSoundGround;
    private RadioGroup rgFileEq;
    private Button beginFileEq;
    private TextView tvSpeed;
    private TextView tvPitch;
    private SeekBar sbSpeed;
    private SeekBar sbPitch;
    private Button beginFileSpeedPitch;
    private Button beginFileReduction;
    private Button beginDevide;
    private CheckBox rbAccompaniment;
    private CheckBox rbVocals;
    private CheckBox rbFiddle;
    private CheckBox rbGuitar;
    private CheckBox rbPiano;
    private CheckBox rbBass;
    private CheckBox rbDrums;
    private EditText etX;
    private EditText etY;
    private EditText etZ;
    private Button beginSpaceRender;

    private volatile boolean isProcessing;

    private static final int TYPE_NONE = 0;
    private static final int TYPE_CHANGE_SOUND = 1;
    private static final int TYPE_ENV = 2;
    private static final int TYPE_SOUND_GROUND = 3;
    private static final int TYPE_EQ = 4;
    private static final int TYPE_SPEED_PITCH = 5;
    private static final int TYPE_REDUCTION = 6;
    private static final int TYPE_SPACE_RENDER = 7;
    private static final int TYPE_DIVIDE = 8;
    private int currentType = TYPE_NONE;
    private List<String> instruments;
    private ProgressDialog progressDialog;

    String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private ChangeSoundCallback callBack =
            new ChangeSoundCallback() {
                @Override
                public void onSuccess(String outAudioPath) {
                    runOnUiThread(
                            () -> {
                                Toast.makeText(FileApiActivity.this, "Success: " + outAudioPath, Toast.LENGTH_SHORT)
                                        .show();
                                isProcessing = false;
                                if (progressDialog != null) {
                                    progressDialog.hide();
                                }
                            });
                }

                @Override
                public void onProgress(int progress) {
                    runOnUiThread(
                            () -> {
                                if (progressDialog != null) {
                                    progressDialog.setProgress(progress);
                                }
                            });
                }

                @Override
                public void onFail(int errorCode) {
                    runOnUiThread(
                            () -> {
                                isProcessing = false;
                                Toast.makeText(FileApiActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT)
                                        .show();
                                if (progressDialog != null) {
                                    progressDialog.hide();
                                }
                            });
                }

                @Override
                public void onCancel() {
                    runOnUiThread(
                            () -> {
                                isProcessing = false;
                                Toast.makeText(FileApiActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                                if (progressDialog != null) {
                                    progressDialog.hide();
                                }
                            });
                }
            };

    private String filePath = "";

    // 倍速
    private float speed = 1.0F;

    // 音调
    private float pitch = 1.0F;

    // 最大的倍速值
    private static final float MAX_SPEED_VALUE = 10.0f;
    private static final int MAX_SPEED_PROGRESS_VALUE = 100;

    // 最小的倍速值
    private static final float MIN_SPEED_VALUE = 0.5f;
    private static final int MIN_SPEED_PROGRESS_VALUE = 0;
    private final float PROGRESS_SPEED_INTERVAL =
            bigDiv(MAX_SPEED_VALUE - MIN_SPEED_VALUE, MAX_SPEED_PROGRESS_VALUE - MIN_SPEED_PROGRESS_VALUE);

    private HAEChangeVoiceFile haeChangeVoiceFile;
    private HAESceneFile haeSceneFile;
    private HAESoundFieldFile haeSoundFieldFile;
    private HAEEqualizerFile haeEqualizerFile;
    private HAETempoPitch haeTempoPitch;
    private HAEAudioSeparationFile haeAudioSeparationFile;
    private HAENoiseReductionFile haeNoiseReductionFile;
    private HAESpaceRenderFile haeSpaceRenderFile;

    private int REQUEST_CODE_FOR_SELECT_AUDIO = 1000;

    private ConstraintLayout constraintLayout;
    private LinearLayout separationDivider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_api);
        constraintLayout = findViewById(R.id.constraintLayout);
        fileBack = findViewById(R.id.back);
        fileBack.setOnClickListener(this);
        separationDivider = findViewById(R.id.group_divider);
        tvFilePath = findViewById(R.id.file_path);
        tvFilePath.setText(filePath);
        choiceFile = findViewById(R.id.choice_file);
        choiceFile.setOnClickListener(this);
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        beginFileChange = findViewById(R.id.begin_change);
        beginFileChange.setOnClickListener(this);
        rgFileSoundType = findViewById(R.id.rg_sound_type);
        rgFileSoundType.setOnCheckedChangeListener(this);
        rgFileEnvType = findViewById(R.id.rg_env_type);
        rgFileEnvType.setOnCheckedChangeListener(this);
        beginFileEvn = findViewById(R.id.begin_env);
        beginFileEvn.setOnClickListener(this);
        rgFileSoundGround = findViewById(R.id.rg_sound_ground_type);
        rgFileSoundGround.setOnCheckedChangeListener(this);
        beginFileSoundGround = findViewById(R.id.begin_sound_ground);
        beginFileSoundGround.setOnClickListener(this);
        beginFileEq = findViewById(R.id.begin_eq);
        beginFileEq.setOnClickListener(this);
        tvSpeed = findViewById(R.id.tv_speed);
        sbSpeed = findViewById(R.id.sb_speed);
        tvPitch = findViewById(R.id.tv_pitch);
        sbPitch = findViewById(R.id.sb_pitch);
        beginFileSpeedPitch = findViewById(R.id.begin_speed_pitch);
        beginFileSpeedPitch.setOnClickListener(this);
        beginFileReduction = findViewById(R.id.begin_reduction);
        beginFileReduction.setOnClickListener(this);
        etX = findViewById(R.id.x);
        etY = findViewById(R.id.y);
        etZ = findViewById(R.id.z);
        beginSpaceRender = findViewById(R.id.begin_space_render);
        beginSpaceRender.setOnClickListener(this);
        beginDevide = findViewById(R.id.begin_devide);
        beginDevide.setOnClickListener(this);

        haeChangeVoiceFile = new HAEChangeVoiceFile();
        haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_SEASONED);

        haeSceneFile = new HAESceneFile();
        haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);

        haeSoundFieldFile = new HAESoundFieldFile();
        haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_WIDE);

        haeEqualizerFile = new HAEEqualizerFile();
        haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_POP_VALUE);

        haeTempoPitch = new HAETempoPitch();
        haeTempoPitch.changeTempoAndPitchOfFile(speed, pitch);

        haeNoiseReductionFile = new HAENoiseReductionFile();

        haeAudioSeparationFile = new HAEAudioSeparationFile();
        instruments = new ArrayList<>();
        haeAudioSeparationFile.getInstruments(
                new SeparationCloudCallBack<List<SeparationBean>>() {
                    @Override
                    public void onFinish(List<SeparationBean> response) {
                        if (response != null && !response.isEmpty()) {
                            for (SeparationBean separationBean : response) {
                                if (!separationBean.getInstrument().equalsIgnoreCase("accomp")) {
                                    continue;
                                }
                                CheckBox cb = new CheckBox(FileApiActivity.this);
                                cb.setText(separationBean.getDesc());
                                cb.setTextColor(getResources().getColor(R.color.white));
                                LinearLayout.LayoutParams cbParam =
                                        new LinearLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.WRAP_CONTENT);
                                cb.setLayoutParams(cbParam);
                                separationDivider.addView(cb);

                                cb.setOnCheckedChangeListener(
                                        new CompoundButton.OnCheckedChangeListener() {
                                            @Override
                                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                if (isChecked) {
                                                    instruments.add(separationBean.getInstrument());
                                                } else {
                                                    instruments.remove(separationBean.getInstrument());
                                                }
                                            }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onError(int errorCode) {}
                });

        haeSpaceRenderFile = new HAESpaceRenderFile();
        haeSpaceRenderFile.setSpacePoint(0, 0, 0);

        initProgress();
        initSeekBar();
    }

    private void initSeekBar() {
        tvSpeed.setText(format(speed) + "x");
        float mSpeedProgress = revertSpeedToProgress(speed);
        sbSpeed.setProgress((int) mSpeedProgress);
        tvPitch.setText("+" + format(pitch));
        float mToneProgress = 10.0F;
        if (pitch <= 0) {
            mToneProgress = 10.0F;
        } else {
            mToneProgress = pitch * 10;
        }
        sbPitch.setProgress((int) mToneProgress);
        if (sbSpeed != null) {
            sbSpeed.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                if (progress > 0) {
                                    BigDecimal bSpeed = new BigDecimal(convertProgressToSpeed(progress));
                                    speed = bSpeed.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                                } else {
                                    speed = MIN_SPEED_VALUE;
                                    sbSpeed.setProgress((int) (speed * PROGRESS_SPEED_INTERVAL));
                                }
                                tvSpeed.setText(format(speed) + "x");
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
        }

        sbPitch.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            if (progress > 0) {
                                pitch = progress / 10.0F;
                                tvPitch.setText("+" + format(pitch));
                            } else {
                                pitch = 0.1F;
                                sbSpeed.setProgress(1);
                                tvPitch.setText("+" + "0.1");
                            }
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
    }

    public static String format(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(1, RoundingMode.HALF_UP);
        return bd.toString();
    }

    private float revertSpeedToProgress(float speed) {
        float curProgress = bigDiv(MIN_SPEED_VALUE, PROGRESS_SPEED_INTERVAL);
        if (speed > 0) {
            curProgress = bigDiv(speed - MIN_SPEED_VALUE, PROGRESS_SPEED_INTERVAL);
        }
        return curProgress;
    }

    private float convertProgressToSpeed(float progress) {
        float curSpeed = MIN_SPEED_VALUE;
        if (progress > 0) {
            curSpeed = bigMul(progress, PROGRESS_SPEED_INTERVAL) + MIN_SPEED_VALUE;
        }
        return curSpeed;
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setTitle(getResources().getString(R.string.in_progress));
        progressDialog.setMax(100);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.begin_change:
                beginDealAudioFile(TYPE_CHANGE_SOUND);
                break;
            case R.id.begin_devide:
                beginDivideAudioFile();
                break;
            case R.id.begin_env:
                beginDealAudioFile(TYPE_ENV);
                break;
            case R.id.begin_sound_ground:
                beginDealAudioFile(TYPE_SOUND_GROUND);
                break;
            case R.id.begin_eq:
                beginDealAudioFile(TYPE_EQ);
                break;
            case R.id.begin_speed_pitch:
                beginDealAudioFile(TYPE_SPEED_PITCH);
                break;
            case R.id.begin_reduction:
                beginDealAudioFile(TYPE_REDUCTION);
                break;
            case R.id.begin_space_render:
                beginDealAudioFile(TYPE_SPACE_RENDER);
                break;
            case R.id.choice_file:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("audio/*");
                startActivityForResult(intent, REQUEST_CODE_FOR_SELECT_AUDIO);
                break;
            case R.id.cancel:
                cancelDeal();
                break;
            default:
                break;
        }
    }

    private void cancelDeal() {
        if (!isProcessing) {
            return;
        }
        if (progressDialog != null) {
            progressDialog.hide();
        }
        if (currentType == TYPE_CHANGE_SOUND) {
            haeChangeVoiceFile.cancel();
        } else if (currentType == TYPE_ENV) {
            haeSceneFile.cancel();
        } else if (currentType == TYPE_SOUND_GROUND) {
            haeSoundFieldFile.cancel();
        } else if (currentType == TYPE_EQ) {
            haeEqualizerFile.cancel();
        } else if (currentType == TYPE_SPEED_PITCH) {
            haeTempoPitch.cancel();
        } else if (currentType == TYPE_REDUCTION) {
            haeNoiseReductionFile.cancel();
        } else if (currentType == TYPE_SPACE_RENDER) {
            haeSpaceRenderFile.cancel();
        } else if (currentType == TYPE_DIVIDE) {
            haeAudioSeparationFile.cancel();
        }
    }

    private void beginDivideAudioFile() {
        isProcessing = true;
        currentType = TYPE_DIVIDE;
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, getResources().getString(R.string.select_none_audio), Toast.LENGTH_SHORT).show();
            return;
        }

        haeAudioSeparationFile.setInstruments(instruments);
        haeAudioSeparationFile.startSeparationTasks(
                filePath,
                outputPath,
                "AudioDivide",
                new AudioSeparationCallBack() {
                    @Override
                    public void onResult(SeparationBean separationBean) {
                        runOnUiThread(
                                () -> {
                                    Toast.makeText(
                                                    FileApiActivity.this,
                                                    separationBean.getInstrument()
                                                            + " Success: "
                                                            + separationBean.getOutAudioPath(),
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    isProcessing = false;
                                    if (progressDialog != null) {
                                        progressDialog.hide();
                                    }
                                });
                    }

                    @Override
                    public void onFinish(List<SeparationBean> separationBeans) {
                        runOnUiThread(
                                () -> {
                                    isProcessing = false;
                                    if (progressDialog != null) {
                                        progressDialog.hide();
                                    }
                                });
                    }

                    @Override
                    public void onFail(int errorCode) {
                        runOnUiThread(
                                () -> {
                                    isProcessing = false;
                                    Toast.makeText(FileApiActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT)
                                            .show();
                                    if (progressDialog != null) {
                                        progressDialog.hide();
                                    }
                                });
                    }

                    @Override
                    public void onCancel() {
                        runOnUiThread(
                                () -> {
                                    isProcessing = false;
                                    Toast.makeText(FileApiActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                                    if (progressDialog != null) {
                                        progressDialog.hide();
                                    }
                                });
                    }
                });

        if (progressDialog != null) {
            progressDialog.setProgress(0);
            progressDialog.show();
        }
    }

    private void beginDealAudioFile(int type) {
        if (isProcessing) {
            return;
        }
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, getResources().getString(R.string.select_none_audio), Toast.LENGTH_SHORT).show();
            return;
        }
        currentType = type;

        isProcessing = true;
        if (progressDialog != null) {
            progressDialog.setProgress(0);
            progressDialog.show();
        }
        if (currentType == TYPE_CHANGE_SOUND) {
            haeChangeVoiceFile.applyAudioFile(filePath, outputPath, "ChangeSound", callBack);
        } else if (currentType == TYPE_ENV) {
            haeSceneFile.applyAudioFile(filePath, outputPath, "EnvironmentChoice", callBack);
        } else if (currentType == TYPE_SOUND_GROUND) {
            haeSoundFieldFile.applyAudioFile(filePath, outputPath, "SoundGround", callBack);
        } else if (currentType == TYPE_EQ) {
            haeEqualizerFile.applyAudioFile(filePath, outputPath, "Equalizer", callBack);
        } else if (currentType == TYPE_SPEED_PITCH) {
            haeTempoPitch.changeTempoAndPitchOfFile(speed, pitch);
            haeTempoPitch.applyAudioFile(filePath, outputPath, "SpeedPitch", callBack);
        } else if (currentType == TYPE_REDUCTION) {
            haeNoiseReductionFile.applyAudioFile(filePath, outputPath, "NoiseReduction", callBack);
        } else if (currentType == TYPE_SPACE_RENDER) {
            if (etX != null && etY != null && etZ != null) {
                float x = 0;
                float y = 0;
                float z = 0;
                try {
                    x = Float.parseFloat(etX.getText().toString());
                    y = Float.parseFloat(etY.getText().toString());
                    z = Float.parseFloat(etZ.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
                haeSpaceRenderFile.setSpacePoint(x, y, z);
            }
            haeSpaceRenderFile.applyAudioFile(filePath, outputPath, "SpaceRender", callBack);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_uncle:
                haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_SEASONED);
                break;
            case R.id.rb_lori:
                haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_CUTE);
                break;
            case R.id.rb_female:
                haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_FEMALE);
                break;
            case R.id.rb_male:
                haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_MALE);
                break;
            case R.id.rb_monsters:
                haeChangeVoiceFile.changeSoundTypeOfFile(SoundType.AUDIO_TYPE_MONSTER);
                break;
            case R.id.rb_gb:
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);
                break;
            case R.id.rb_tel:
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_EARPIECE);
                break;
            case R.id.rb_sx:
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_UNDERWATER);
                break;
            case R.id.rb_cd:
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_GRAMOPHONE);
                break;
            case R.id.rb_sound_0:
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_WIDE);
                break;
            case R.id.rb_sound_1:
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_FRONT_FACING);
                break;
            case R.id.rb_sound_2:
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_NEAR);
                break;
            case R.id.rb_sound_3:
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_GRAND);
                break;
            case R.id.rb_pops:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_POP_VALUE);
                break;
            case R.id.rb_classic:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_CLASSICAL_VALUE);
                break;
            case R.id.rb_jazz:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_JAZZ_VALUE);
                break;
            case R.id.rb_rock:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_ROCK_VALUE);
                break;
            case R.id.rb_rb:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_RB_VALUE);
                break;
            case R.id.rb_ballads:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_BALLADS_VALUE);
                break;
            case R.id.rb_dance_music:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_DANCE_MUSIC_VALUE);
                break;
            case R.id.rb_chinese_style:
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_CHINESE_STYLE_VALUE);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_SELECT_AUDIO) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, getResources().getString(R.string.select_none_audio), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    String path = FileUtils.getRealPath(this, uri);
                    if (!TextUtils.isEmpty(path)) {
                        this.filePath = path;
                        if (tvFilePath != null) {
                            tvFilePath.setText(filePath);
                        }
                    }
                }
            }
        }
    }

    private float bigDiv(float v1, float v2) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return b1.divide(b2, 5, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    private float bigMul(float v1, float v2) {
        BigDecimal b1 = new BigDecimal(Float.toString(v1));
        BigDecimal b2 = new BigDecimal(Float.toString(v2));
        return b1.multiply(b2).floatValue();
    }
}
