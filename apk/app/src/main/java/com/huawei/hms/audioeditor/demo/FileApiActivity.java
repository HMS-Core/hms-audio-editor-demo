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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.sdk.AudioParameters;
import com.huawei.hms.audioeditor.sdk.AudioSeparationCallBack;
import com.huawei.hms.audioeditor.sdk.AudioSeparationType;
import com.huawei.hms.audioeditor.sdk.ChangeSoundCallback;
import com.huawei.hms.audioeditor.sdk.ChangeVoiceOption;
import com.huawei.hms.audioeditor.sdk.HAEAudioSeparationFile;
import com.huawei.hms.audioeditor.sdk.HAEChangeVoiceFile;
import com.huawei.hms.audioeditor.sdk.HAEEqualizerFile;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.HAELocalAudioSeparationFile;
import com.huawei.hms.audioeditor.sdk.HAENoiseReductionFile;
import com.huawei.hms.audioeditor.sdk.HAESceneFile;
import com.huawei.hms.audioeditor.sdk.HAESoundFieldFile;
import com.huawei.hms.audioeditor.sdk.HAETempoPitch;
import com.huawei.hms.audioeditor.sdk.bean.SeparationBean;
import com.huawei.hms.audioeditor.sdk.materials.network.SeparationCloudCallBack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * File Interface
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

    // Cloud-side audio source separation, separation type
    private List<String> instruments;

    // Device-side audio source separation, separation type
    private List<String> localInstruments;
    private ProgressDialog progressDialog;

    private RadioButton mRbMan;
    private RadioButton mRbWoman;
    private RadioButton mRbFemale;
    private RadioButton mRbMale;

    private String currentName = "";

    String outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath()+"/AudioEdit/sample";

    private ChangeSoundCallback callBack = new ChangeSoundCallback() {
        @Override
        public void onSuccess(String outAudioPath) {
            runOnUiThread(() -> {
                Toast.makeText(FileApiActivity.this, "Success: " + outAudioPath, Toast.LENGTH_SHORT).show();
                isProcessing = false;
                hideProgress();
            });
        }

        @Override
        public void onProgress(int progress) {
            runOnUiThread(() -> {
                if (progressDialog != null) {
                    progressDialog.setProgress(progress);
                }
            });
        }

        @Override
        public void onFail(int errorCode) {
            runOnUiThread(() -> {
                isProcessing = false;
                hideProgress();
                if (errorCode == HAEErrorCode.FAIL_FILE_EXIST) {
                    Toast.makeText(FileApiActivity.this, getResources().getString(R.string.file_exists),
                            Toast.LENGTH_LONG).show();
                    EditDialogFragment.newInstance("", currentName, (newName, dialog) -> {
                        realDealAudioFile(newName);
                        dialog.dismiss();
                    }).show(getSupportFragmentManager(), "EditDialogFragment");
                } else {
                    Toast.makeText(FileApiActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onCancel() {
            runOnUiThread(() -> {
                isProcessing = false;
                Toast.makeText(FileApiActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                hideProgress();
            });
        }
    };

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    private String filePath = "";

    // double speed
    private float speed = 1.0F;

    // tones
    private float pitch = 1.0F;

    // Maximum speed
    private static final float MAX_SPEED_VALUE = 10.0f;
    private static final int MAX_SPEED_PROGRESS_VALUE = 100;

    private ChangeVoiceOption changeVoiceOption;

    // Minimum value of multiple speed
    private static final float MIN_SPEED_VALUE = 0.5f;
    private static final int MIN_SPEED_PROGRESS_VALUE = 0;
    private final float PROGRESS_SPEED_INTERVAL = bigDiv(MAX_SPEED_VALUE - MIN_SPEED_VALUE,
            MAX_SPEED_PROGRESS_VALUE - MIN_SPEED_PROGRESS_VALUE);

    private HAEChangeVoiceFile haeChangeVoiceFile;
    private HAESceneFile haeSceneFile;
    private HAESoundFieldFile haeSoundFieldFile;
    private HAEEqualizerFile haeEqualizerFile;
    private HAETempoPitch haeTempoPitch;
    private HAEAudioSeparationFile haeAudioSeparationFile;
    private HAENoiseReductionFile haeNoiseReductionFile;
    private HAELocalAudioSeparationFile audioSeparationFile;

    private int REQUEST_CODE_FOR_SELECT_AUDIO = 1000;

    private ConstraintLayout constraintLayout;
    private LinearLayout separationDivider;

    private RadioGroup rgSoundSex;
    private RadioGroup rgSoundPart;
    private SeekBar mSbTones;
    private TextView mTvSeekValue1;

    private float[] malePitch = {0.8f, 2.3f, 1.9f, 1.2f, 0.7f, 1.0f, 1.5f};
    private float[] femalePitch = {0.5f, 1.4f, 1f, 0.6f, 0.4f, 1.0f, 1.1f};

    private int currentVoiceType = ChangeVoiceOption.VoiceType.SEASONED.ordinal();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_api);
        initView();
        initAllAbility();
        initProgress();
        initSeekBar();
    }

    private void initView() {
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
        rgFileEq = findViewById(R.id.rg_eq_type);
        rgFileEq.setOnCheckedChangeListener(this);
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
        beginDevide = findViewById(R.id.begin_devide);
        beginDevide.setOnClickListener(this);
        findViewById(R.id.begin_vocals_devide).setOnClickListener(this);

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

        localInstruments = new ArrayList<>();
        // default
        localInstruments.add(AudioSeparationType.VOCALS);
        rbVocals = findViewById(R.id.rbVocals);
        rbVocals.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    localInstruments.add(AudioSeparationType.VOCALS);
                } else {
                    localInstruments.remove(AudioSeparationType.VOCALS);
                }
            }
        });

        CheckBox rbGuitar = findViewById(R.id.rbGuitar);
        rbGuitar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    localInstruments.add(AudioSeparationType.AGUITAR);
                } else {
                    localInstruments.remove(AudioSeparationType.AGUITAR);
                }
            }
        });

        CheckBox rbElectricGuitar = findViewById(R.id.rbElectricGuitar);
        rbElectricGuitar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    localInstruments.add(AudioSeparationType.EGUITAR);
                } else {
                    localInstruments.remove(AudioSeparationType.EGUITAR);
                }
            }
        });

        CheckBox rbPiano = findViewById(R.id.rbPiano);
        rbPiano.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    localInstruments.add(AudioSeparationType.PIANO);
                } else {
                    localInstruments.remove(AudioSeparationType.PIANO);
                }
            }
        });
    }

    private void initAllAbility() {
        haeChangeVoiceFile = new HAEChangeVoiceFile();
        changeVoiceOption = new ChangeVoiceOption();
        changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.MALE);
        // By default, the parameter selected on the UI is Seasoned.
        changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.SEASONED);
        haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
        resetpitch();

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
        haeAudioSeparationFile.getInstruments(new SeparationCloudCallBack<List<SeparationBean>>() {
            @Override
            public void onFinish(List<SeparationBean> response) {
                if (response != null && !response.isEmpty()) {
                    for (SeparationBean separationBean : response) {
                        CheckBox cb = new CheckBox(FileApiActivity.this);
                        cb.setText(separationBean.getDesc());
                        cb.setTextColor(getResources().getColor(R.color.white));
                        LinearLayout.LayoutParams cbParam = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        cb.setLayoutParams(cbParam);
                        separationDivider.addView(cb);

                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
            public void onError(int errorCode) {
            }
        });

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
            sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
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
            case R.id.back :
                onBackPressed();
                break;
            case R.id.begin_change :
                beginDealAudioFile(TYPE_CHANGE_SOUND);
                break;
            case R.id.begin_devide :
                beginDivideAudioFile();
                break;
            case R.id.begin_vocals_devide :
                beginLocalDivideAudioFile();
                break;
            case R.id.begin_env :
                beginDealAudioFile(TYPE_ENV);
                break;
            case R.id.begin_sound_ground :
                beginDealAudioFile(TYPE_SOUND_GROUND);
                break;
            case R.id.begin_eq :
                beginDealAudioFile(TYPE_EQ);
                break;
            case R.id.begin_speed_pitch :
                beginDealAudioFile(TYPE_SPEED_PITCH);
                break;
            case R.id.begin_reduction :
                beginDealAudioFile(TYPE_REDUCTION);
                break;
            case R.id.choice_file :
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                intent.setType("audio/*");
                startActivityForResult(intent, REQUEST_CODE_FOR_SELECT_AUDIO);
                break;
            case R.id.cancel :
                cancelDeal();
                break;
            default :
                break;
        }
    }

    private void cancelDeal() {

        if (audioSeparationFile != null) {
            audioSeparationFile.cancel();
        }
        if (!isProcessing) {
            return;
        }
        hideProgress();
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
        String name = getOrgName() + "_AudioDivide";
        realDivideAudio(name);
    }

    private void beginLocalDivideAudioFile() {
        isProcessing = true;
        currentType = TYPE_DIVIDE;
        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, getResources().getString(R.string.select_none_audio), Toast.LENGTH_SHORT).show();
            return;
        }
        String name = getOrgName();
        realLocalDivideAudio(name);
    }

    private void realDivideAudio(String name) {
        showProgress();
        haeAudioSeparationFile.setInstruments(instruments);
        haeAudioSeparationFile.startSeparationTasks(filePath, outputPath, name, new AudioSeparationCallBack() {
            @Override
            public void onResult(SeparationBean separationBean) {
                runOnUiThread(() -> {
                    Toast.makeText(FileApiActivity.this,
                            separationBean.getInstrument() + " Success: " + separationBean.getOutAudioPath(),
                            Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                    hideProgress();
                });
            }

            @Override
            public void onFinish(List<SeparationBean> separationBeans) {
                runOnUiThread(() -> {
                    isProcessing = false;
                    hideProgress();
                });
            }

            @Override
            public void onFail(int errorCode) {
                runOnUiThread(() -> {

                    isProcessing = false;
                    hideProgress();
                    if (errorCode != HAEErrorCode.FAIL_FILE_EXIST) {
                        Toast.makeText(FileApiActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FileApiActivity.this, getResources().getString(R.string.file_exists),
                                Toast.LENGTH_LONG).show();
                        EditDialogFragment.newInstance("", name, (newName, dialog) -> {
                            realDivideAudio(newName);
                            dialog.dismiss();
                        }).show(getSupportFragmentManager(), "EditDialogFragment");
                    }
                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(() -> {
                    isProcessing = false;
                    Toast.makeText(FileApiActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                    hideProgress();
                });
            }
        });
    }

    // Voice separation function of the terminal test version
    private void realLocalDivideAudio(String name) {
        showProgress();
        audioSeparationFile = new HAELocalAudioSeparationFile();
        audioSeparationFile.setInstruments(localInstruments);
        audioSeparationFile.startSeparationTask(filePath, outputPath, name, new AudioSeparationCallBack() {
            @Override
            public void onResult(SeparationBean separationBean) {
                runOnUiThread(() -> {
                    Toast.makeText(FileApiActivity.this,
                            separationBean.getInstrument() + " Success: " + separationBean.getOutAudioPath(),
                            Toast.LENGTH_SHORT).show();
                    isProcessing = false;
                });

            }

            @Override
            public void onFinish(List<SeparationBean> separationBeans) {
                runOnUiThread(() -> {
                    isProcessing = false;
                    hideProgress();
                });

            }

            @Override
            public void onFail(int errorCode) {
                runOnUiThread(() -> {

                    isProcessing = false;
                    hideProgress();
                    if (errorCode != HAEErrorCode.FAIL_FILE_EXIST) {
                        Toast.makeText(FileApiActivity.this, "ErrorCode : " + errorCode, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(FileApiActivity.this, getResources().getString(R.string.file_exists),
                                Toast.LENGTH_LONG).show();
                        EditDialogFragment.newInstance("", name, (newName, dialog) -> {
                            realLocalDivideAudio(newName);
                            dialog.dismiss();
                        }).show(getSupportFragmentManager(), "EditDialogFragment");
                    }
                });
            }

            @Override
            public void onCancel() {
                runOnUiThread(() -> {
                    isProcessing = false;
                    Toast.makeText(FileApiActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                    hideProgress();
                });
            }
        });

    }

    private void showProgress() {
        if (progressDialog != null) {
            initProgress();
            if (currentType == TYPE_DIVIDE) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            } else {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
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
        String name = getOrgName();
        realDealAudioFile(name);
    }

    private String getOrgName() {
        String name = "fileApi";
        int lastIndexOf = filePath.lastIndexOf("/");
        if (lastIndexOf > -1) {
            name = filePath.substring(lastIndexOf + 1);
        }
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex > -1) {
            name = name.substring(0, dotIndex);
        }
        return name;
    }

    private void realDealAudioFile(String name) {
        isProcessing = true;
        showProgress();
        currentName = name;
        if (currentType == TYPE_CHANGE_SOUND) {
            haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
            haeChangeVoiceFile.applyAudioFile(filePath, outputPath, currentName, callBack);
        } else if (currentType == TYPE_ENV) {
            haeSceneFile.applyAudioFile(filePath, outputPath, currentName, callBack);
        } else if (currentType == TYPE_SOUND_GROUND) {
            haeSoundFieldFile.applyAudioFile(filePath, outputPath, currentName, callBack);
        } else if (currentType == TYPE_EQ) {
            haeEqualizerFile.applyAudioFile(filePath, outputPath, currentName, callBack);
        } else if (currentType == TYPE_SPEED_PITCH) {
            haeTempoPitch.changeTempoAndPitchOfFile(speed, pitch);
            haeTempoPitch.applyAudioFile(filePath, outputPath, currentName, callBack);
        } else if (currentType == TYPE_REDUCTION) {
            haeNoiseReductionFile.applyAudioFile(filePath, outputPath, currentName, callBack);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_uncle :
                currentVoiceType = ChangeVoiceOption.VoiceType.SEASONED.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.SEASONED);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_lori :
                currentVoiceType = ChangeVoiceOption.VoiceType.CUTE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.CUTE);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_female :
                currentVoiceType = ChangeVoiceOption.VoiceType.FEMALE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.FEMALE);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_male :
                currentVoiceType = ChangeVoiceOption.VoiceType.MALE.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.MALE);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_monsters :
                currentVoiceType = ChangeVoiceOption.VoiceType.MONSTER.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.MONSTER);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_robots :
                currentVoiceType = ChangeVoiceOption.VoiceType.ROBOTS.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.ROBOTS);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_cartoon :
                currentVoiceType = ChangeVoiceOption.VoiceType.CARTOON.ordinal();
                changeVoiceOption.setVoiceType(ChangeVoiceOption.VoiceType.CARTOON);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_gb :
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_BROADCAST);
                break;
            case R.id.rb_tel :
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_EARPIECE);
                break;
            case R.id.rb_sx :
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_UNDERWATER);
                break;
            case R.id.rb_cd :
                haeSceneFile.setTypeOfFile(AudioParameters.ENVIRONMENT_TYPE_GRAMOPHONE);
                break;
            case R.id.rb_sound_0 :
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_WIDE);
                break;
            case R.id.rb_sound_1 :
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_FRONT_FACING);
                break;
            case R.id.rb_sound_2 :
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_NEAR);
                break;
            case R.id.rb_sound_3 :
                haeSoundFieldFile.setTypeOfFile(AudioParameters.SOUND_FIELD_GRAND);
                break;
            case R.id.rb_pops :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_POP_VALUE);
                break;
            case R.id.rb_classic :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_CLASSICAL_VALUE);
                break;
            case R.id.rb_jazz :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_JAZZ_VALUE);
                break;
            case R.id.rb_rock :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_ROCK_VALUE);
                break;
            case R.id.rb_rb :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_RB_VALUE);
                break;
            case R.id.rb_ballads :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_BALLADS_VALUE);
                break;
            case R.id.rb_dance_music :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_DANCE_MUSIC_VALUE);
                break;
            case R.id.rb_chinese_style :
                haeEqualizerFile.setEqValueOfFile(AudioParameters.EQUALIZER_CHINESE_STYLE_VALUE);
                break;
            case R.id.rb_man :
                changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.MALE);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
                resetpitch();
                break;
            case R.id.rb_woman :
                changeVoiceOption.setSpeakerSex(ChangeVoiceOption.SpeakerSex.FEMALE);
                haeChangeVoiceFile.changeVoiceOption(changeVoiceOption);
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
