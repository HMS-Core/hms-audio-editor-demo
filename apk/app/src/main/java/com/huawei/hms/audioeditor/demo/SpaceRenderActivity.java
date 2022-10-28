/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.audioeditor.demo.util.FileUtils;
import com.huawei.hms.audioeditor.demo.widget.EditDialogFragment;
import com.huawei.hms.audioeditor.demo.widget.SpaceRenderPositionView;
import com.huawei.hms.audioeditor.sdk.ChangeSoundCallback;
import com.huawei.hms.audioeditor.sdk.HAEErrorCode;
import com.huawei.hms.audioeditor.sdk.HAESpaceRenderFile;
import com.huawei.hms.audioeditor.sdk.SpaceRenderExtensionParams;
import com.huawei.hms.audioeditor.sdk.SpaceRenderMode;
import com.huawei.hms.audioeditor.sdk.SpaceRenderPositionParams;
import com.huawei.hms.audioeditor.sdk.SpaceRenderRotationParams;

public class SpaceRenderActivity extends AppCompatActivity
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private static final String TAG = "SpaceRenderActivity";
    private static final int REQUEST_CODE_FOR_SELECT_AUDIO = 1000;
    private static final int MODEL_ERROR = 1007;
    public Switch mFixedPositioning;
    public Switch mSwitchDT;
    public Switch mSwitchKZ;
    public LinearLayout mLlPosition;
    private LinearLayout mLlDT;
    private LinearLayout mLlSeekbar;
    private LinearLayout mLlKZ;
    private Button choiceFile;
    private Button cancel;
    private String filePath = "";
    private TextView tvFilePath;
    private SeekBar mSbSurround;
    private SeekBar mSbRadius;
    private SeekBar mSbAngled;
    private int surroundVal = 2;
    private float radiusVal = 1.0f;
    private int angledVal = 90;
    private TextView mTvSurround;
    private TextView mTvRadius;
    private TextView mTVangled;
    private SpaceRenderPositionView renderFront;
    private SpaceRenderPositionView renderTop;
    private SpaceRenderPositionView renderFront1;
    private SpaceRenderPositionView renderTop1;
    private Button mStartRendering;
    private String outputPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    private String currentName = "";
    private String typeName = "";
    private ProgressDialog progressDialog;
    private volatile boolean isProcessing;
    private HAESpaceRenderFile haeSpaceRenderFile;
    private TextView mBack;

    private RadioGroup mRgClock;
    private int mClockwise = SpaceRenderRotationParams.CW;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_space_render);
        initView();
        initEvents();
        initProgress();
        haeSpaceRenderFile = new HAESpaceRenderFile(SpaceRenderMode.POSITION);
    }

    private void initView() {
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mFixedPositioning = findViewById(R.id.switch_fixed_positioning);
        mSwitchDT = findViewById(R.id.switch_dt);
        mSwitchKZ = findViewById(R.id.switch_kz);
        mLlPosition = findViewById(R.id.ll_position);
        mLlDT = findViewById(R.id.ll_dt);
        mLlKZ = findViewById(R.id.ll_kz);
        mLlSeekbar = findViewById(R.id.ll_seekbar);
        choiceFile = findViewById(R.id.choice_file);
        choiceFile.setOnClickListener(this);
        tvFilePath = findViewById(R.id.file_path);
        tvFilePath.setText(filePath);
        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        mSbSurround = findViewById(R.id.sb_1);
        mSbRadius = findViewById(R.id.sb_2);
        mSbAngled = findViewById(R.id.sb_3);
        // 2-40
        mSbSurround.setMax(38);
        // 0-5
        mSbRadius.setMax(5);
        // 0-360
        mSbAngled.setMax(360);

        mTvSurround = findViewById(R.id.tv_value_2);
        mTvRadius = findViewById(R.id.tv_value_3);
        mTVangled = findViewById(R.id.tv_value);
        mTvSurround.setText("2");
        mTvRadius.setText("1.0");
        mTVangled.setText("90");

        renderFront = findViewById(R.id.render_front);
        renderTop = findViewById(R.id.render_top);
        renderFront.setBrother(renderTop);
        renderTop.setBrother(renderFront);

        renderFront1 = findViewById(R.id.render_front_1);
        renderTop1 = findViewById(R.id.render_top_1);
        renderFront1.setBrother(renderTop1);
        renderTop1.setBrother(renderFront1);

        mStartRendering = findViewById(R.id.start_rendering);
        mStartRendering.setOnClickListener(this);

        mRgClock = findViewById(R.id.rg_clock);
        mRgClock.setOnCheckedChangeListener(this);
    }

    private void initEvents() {
        mFixedPositioning.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if (b) {
                        mLlPosition.setVisibility(View.VISIBLE);
                        mSwitchDT.setChecked(false);
                        mSwitchKZ.setChecked(false);
                    } else {
                        mLlPosition.setVisibility(View.GONE);
                    }
                });
        mSwitchDT.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if (b) {
                        mLlDT.setVisibility(View.VISIBLE);
                        mLlSeekbar.setVisibility(View.VISIBLE);
                        mRgClock.setVisibility(View.VISIBLE);
                        mFixedPositioning.setChecked(false);
                        mSwitchKZ.setChecked(false);
                    } else {
                        mLlDT.setVisibility(View.GONE);
                        mLlSeekbar.setVisibility(View.GONE);
                        mRgClock.setVisibility(View.GONE);
                    }
                });
        mSwitchKZ.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if (b) {
                        mLlKZ.setVisibility(View.VISIBLE);
                        mSwitchDT.setChecked(false);
                        mFixedPositioning.setChecked(false);
                    } else {
                        mLlKZ.setVisibility(View.GONE);
                    }
                });
        mSbSurround.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        surroundVal = i + 2;
                        mTvSurround.setText(surroundVal + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
        mSbRadius.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        radiusVal = i;
                        if (radiusVal == 0) {
                            radiusVal = 0.1f;
                        }
                        mTvRadius.setText(radiusVal + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

        mSbAngled.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        angledVal = i;
                        mTVangled.setText(angledVal + "");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
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
            case R.id.start_rendering:
                if(isProcessing){
                    if (progressDialog != null && !progressDialog.isShowing() ){
                        progressDialog.show();
                    }
                    return;
                }
                if (filePath.equals("")) {
                    Toast.makeText(
                                    SpaceRenderActivity.this,
                                    this.getResources().getString(R.string.select_none_audio),
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (!mFixedPositioning.isChecked() && !mSwitchDT.isChecked() && !mSwitchKZ.isChecked()) {
                    Toast.makeText(
                                    SpaceRenderActivity.this,
                                    this.getResources().getString(R.string.not_select_type),
                                    Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                showProgress();
                if (mFixedPositioning.isChecked()) {
                    typeName = "position-";
                    float orientationX = renderFront.getOrientationX();
                    float orientationY = renderFront.getOrientationY();
                    float orientationZ = renderFront.getOrientationZ();
                    haeSpaceRenderFile.changeSpaceRenderMode(SpaceRenderMode.POSITION);
                    haeSpaceRenderFile.setSpacePositionParams(
                            new SpaceRenderPositionParams(orientationX, orientationY, orientationZ));
                }
                if (mSwitchDT.isChecked()) {
                    typeName = "rotation-";
                    float orientationX = renderFront1.getOrientationX();
                    float orientationY = renderFront1.getOrientationY();
                    float orientationZ = renderFront1.getOrientationZ();
                    haeSpaceRenderFile.changeSpaceRenderMode(SpaceRenderMode.ROTATION);
                    haeSpaceRenderFile.setRotationParams(
                            new SpaceRenderRotationParams(
                                    orientationX, orientationY, orientationZ, surroundVal, mClockwise));
                }
                if (mSwitchKZ.isChecked()) {
                    typeName = "extension-";
                    haeSpaceRenderFile.changeSpaceRenderMode(SpaceRenderMode.EXTENSION);
                    haeSpaceRenderFile.setExtensionParams(new SpaceRenderExtensionParams(radiusVal, angledVal));
                }
                isProcessing = true;
                currentName = typeName + getOrgName();
                haeSpaceRenderFile.applyAudioFile(filePath, outputPath, currentName, callBack);
                break;
            default:
                break;
        }
    }

    private final ChangeSoundCallback callBack =
            new ChangeSoundCallback() {
                @Override
                public void onSuccess(String outAudioPath) {
                    // success
                    runOnUiThread(
                            () -> {
                                Toast.makeText(SpaceRenderActivity.this, outAudioPath, Toast.LENGTH_SHORT).show();
                                isProcessing = false;
                                hideProgress();

                            });
                }

                @Override
                public void onProgress(int progress) {
                    // Progress callback processing
                    runOnUiThread(
                            () -> {
                                if (progressDialog != null) {
                                    progressDialog.setProgress(progress);
                                }
                            });
                }

                @Override
                public void onFail(int errorCode) {
                    // Processing failed.
                    runOnUiThread(
                            () -> {
                                isProcessing = false;
                                hideProgress();
                                if (errorCode == HAEErrorCode.FAIL_FILE_EXIST) {
                                    Toast.makeText(
                                                    SpaceRenderActivity.this,
                                                    getResources().getString(R.string.file_exists),
                                                    Toast.LENGTH_LONG)
                                            .show();
                                    EditDialogFragment.newInstance(
                                                    "",
                                                    currentName,
                                                    (newName, dialog) -> {
                                                        realDealAudioFile(newName);
                                                        dialog.dismiss();
                                                    })
                                            .show(getSupportFragmentManager(), "EditDialogFragment");
                                } else {
                                    if (errorCode == MODEL_ERROR){
                                        Toast.makeText(
                                                SpaceRenderActivity.this,
                                                R.string.error_1007,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }else {
                                        Toast.makeText(
                                                SpaceRenderActivity.this,
                                                "ErrorCode : " + errorCode,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
                }

                @Override
                public void onCancel() {
                    // Cancel processing
                    runOnUiThread(
                            () -> {
                                isProcessing = false;
                                Toast.makeText(SpaceRenderActivity.this, "Cancel !", Toast.LENGTH_SHORT).show();
                                hideProgress();
                            });
                }
            };

    private void cancelDeal() {
        if (haeSpaceRenderFile != null) {
            haeSpaceRenderFile.cancel();
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

    private void showProgress() {
        if (progressDialog != null) {
            progressDialog.setProgress(0);
            progressDialog.show();
        }
    }

    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.hide();
        }
    }

    private void initProgress() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.setTitle(getResources().getString(R.string.in_progress));
        progressDialog.setMax(100);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.rb_clockwise:
                mClockwise = SpaceRenderRotationParams.CW;
                break;
            case R.id.rb_counterclockwise:
                mClockwise = SpaceRenderRotationParams.CCW;
                break;
            default:
                break;
        }
    }

    private void realDealAudioFile(String name) {
        isProcessing = true;
        showProgress();
        currentName = name;
        currentName = name + (currentName.contains("_SpaceRender") ? "" : "_SpaceRender");
        haeSpaceRenderFile.applyAudioFile(filePath, outputPath, currentName, callBack);
    }
}
